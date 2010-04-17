/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: XmppAccount.java 
 *
 * @author yinqiwen [ 2010-1-31 | 10:50:02 AM]
 *
 */
package com.hyk.proxy.gae.client.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.client.config.Config;
import com.hyk.proxy.gae.client.httpserver.HttpServer;
import com.hyk.proxy.gae.client.util.ClientUtils;
import com.hyk.proxy.gae.client.util.FetchServiceSelector;
import com.hyk.proxy.gae.common.http.header.ContentRangeHeaderValue;
import com.hyk.proxy.gae.common.http.header.RangeHeaderValue;
import com.hyk.proxy.gae.common.http.message.HttpRequestExchange;
import com.hyk.proxy.gae.common.http.message.HttpResponseExchange;
import com.hyk.rpc.core.RpcCallback;
import com.hyk.rpc.core.RpcCallbackResult;
import com.hyk.rpc.core.Rpctimeout;
import com.hyk.util.buffer.ByteArray;

/**
 * @author yinqiwen
 * 
 */
@ChannelPipelineCoverage("one")
public class HttpRequestHandler extends SimpleChannelUpstreamHandler implements RpcCallback<HttpResponseExchange>
{
	protected Logger				logger			= LoggerFactory.getLogger(getClass());

	private SSLContext				sslContext;
	private volatile HttpRequest	request;
	private volatile boolean		readingChunks;
	private ChannelPipeline			channelPipeline;

	private boolean					ishttps			= false;
	private String					httpspath		= null;

	private FetchServiceSelector fetchServiceSelector;
	private HttpServer				httpServer;
	
	private Executor workerExecutor;
	
	private HttpRequestExchange forwardRequest;
	private HttpRequestExchange originalRequest;
	
	private ContentRangeHeaderValue lastContentRange = null;
	private ChannelBuffer leftChannelBuffer;
	private BlockingQueue<ChannelBuffer> proxyRequestBody = new LinkedBlockingQueue<ChannelBuffer>();

	private Channel	channel;
	private ChunkedInput chunkedInput;

	public HttpRequestHandler(SSLContext sslContext, ChannelPipeline channelPipeline, FetchServiceSelector selector,
			HttpServer httpServer, Executor workerExecutor)
	{
		this.sslContext = sslContext;
		this.channelPipeline = channelPipeline;
		this.fetchServiceSelector = selector;
		this.httpServer = httpServer;
		this.workerExecutor = workerExecutor;
	}

	protected HttpResponseExchange fetch() throws InterruptedException
	{
	    return fetch(false);	
	}
	
	protected HttpResponseExchange fetch(boolean isAsync) throws InterruptedException
	{
		waitForwardBodyComplete();
		if(logger.isDebugEnabled())
		{
			logger.debug("Send proxy request");
			logger.debug(forwardRequest.toPrintableString());
		}
		HttpResponseExchange forwardResponse = null;
		if(!isAsync)
		{
			int retry = 1;
			while((null == forwardResponse) && retry > 0)
			{
				try 
				{
					forwardResponse = fetchServiceSelector.select().fetch(forwardRequest);
				} 
				catch (Rpctimeout e) 
				{
					if(logger.isDebugEnabled())
					{
						logger.debug("Fetch encounter timeout, retry one time.");
					}
				}
				retry--;
			}
			if(null == forwardResponse)
			{
				forwardResponse = new HttpResponseExchange();
				forwardResponse.setResponseCode(408);
			}
			if(logger.isDebugEnabled())
			{
				logger.debug("Recv proxy response");
				logger.debug(forwardResponse.toPrintableString());
			}
		}
		else
		{
			fetchServiceSelector.selectAsync().fetch(forwardRequest, this);
		}
		
		return forwardResponse;
	}

	protected void waitForwardBodyComplete() throws InterruptedException
	{
		int contentLength = forwardRequest.getContentLength();
		if(contentLength > 0 && forwardRequest.getBody() == null)
		{
			byte[] body = new byte[contentLength];
			int cur = 0;
			int end = body.length;
			while(cur < end)
			{
				int reading = end- cur;
				ChannelBuffer buffer = null;
				if(null != leftChannelBuffer)
				{
					buffer = leftChannelBuffer;
					leftChannelBuffer = null;
				}
				else
				{
					buffer = proxyRequestBody.take();
				}
				if(buffer.readableBytes() > reading)
				{
					buffer.readBytes(body, cur, reading);
					leftChannelBuffer = buffer;
					cur += reading;
					break;
				}
				int len = buffer.readableBytes();
				buffer.readBytes(body, cur, buffer.readableBytes());
				cur += len;
			}
			forwardRequest.setBody(body);
		}
	}
		
	protected HttpRequestExchange buildForwardRequest(HttpRequest recvReq) throws IOException, InterruptedException
	{
		HttpRequestExchange gaeRequest = new HttpRequestExchange();
		StringBuffer urlbuffer = new StringBuffer();
		if(ishttps)
		{
			urlbuffer.append("https://").append(httpspath);
		}
		else
		{
			if(!recvReq.getUri().toLowerCase().startsWith("http:"))
			{
				urlbuffer.append("http:").append(recvReq.getHeader(HttpHeaders.Names.HOST));
			}
		}
		urlbuffer.append(recvReq.getUri());
		gaeRequest.setUrl(urlbuffer.toString());
		gaeRequest.setMethod(recvReq.getMethod().getName());
		Set<String> headers = recvReq.getHeaderNames();
		for(String headerName : headers)
		{
			List<String> headerValues = recvReq.getHeaders(headerName);
			if(null != headerValues)
			{
				for(String headerValue : headerValues)
				{
					gaeRequest.addHeader(headerName, headerValue);
				}
			}
		}
		
		int fetchLimit = Config.getInstance().getFetchLimitSize();
		
		//if(recvReq.getContentLength() > fetchLimit)
		if(recvReq.getContentLength() > 1024000) //GAE's limit 1M
		{
			lastContentRange =  new ContentRangeHeaderValue(0, fetchLimit-1, recvReq.getContentLength());
			gaeRequest.setHeader(HttpHeaders.Names.CONTENT_RANGE, lastContentRange);
			gaeRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(fetchLimit));
		}
		ChannelBuffer contentBody = recvReq.getContent();
		if(null != contentBody)
		{
			proxyRequestBody.put(contentBody);
		}

//		int bodyLen = gaeRequest.getContentLength();
//
//		if(bodyLen > 0)
//		{
//			byte[] payload = new byte[bodyLen];
//			ChannelBuffer body = recvReq.getContent();
//			body.readBytes(payload);
//			gaeRequest.setBody(payload);
//		}
		return gaeRequest;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception
	{
		if(!readingChunks)
		{
			this.request = (HttpRequest)e.getMessage();
			if(logger.isDebugEnabled())
			{
				logger.debug(request.getMethod() + " " + request.getUri());
			}
			if(request.getMethod().equals(HttpMethod.CONNECT))
			{
				ishttps = true;
				httpspath = request.getHeader("Host");
				HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
				e.getChannel().write(response);
				//https connection
				if(channelPipeline.get("ssl") == null)
				{
					InetSocketAddress remote = (InetSocketAddress)e.getRemoteAddress();
					SSLEngine engine = sslContext.createSSLEngine(remote.getAddress().getHostAddress(), remote.getPort());
					engine.setUseClientMode(false);
					channelPipeline.addBefore("decoder", "ssl", new SslHandler(engine));
				}
				return;
			}
			else if(request.getMethod().equals(HttpMethod.OPTIONS))
			{
				if(request.getUri().equals(Config.STOP_COMMAND))
				{
					if(logger.isInfoEnabled())
					{
						logger.info("Received a close command to close local server.");
					}
					httpServer.stop();
					System.exit(1);
				}
				return;
			}
			if(request.isChunked())
			{
				readingChunks = true;
			}
			this.forwardRequest = buildForwardRequest(request);
			this.originalRequest = forwardRequest.clone(); 
			this.channel = e.getChannel();
			processProxyRequest();
		}
		else
		{
			HttpChunk chunk = (HttpChunk)e.getMessage();
			if(chunk.isLast())
			{
				readingChunks = false;	
			}
			else
			{
				proxyRequestBody.put(chunk.getContent());;
			}
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		if(chunkedInput != null)
		{
			chunkedInput.close();
		}
		super.channelClosed(ctx, e);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
	{
		logger.error("exceptionCaught.", e.getCause());
		if(chunkedInput != null)
		{
			chunkedInput.close();
		}
		if(e.getChannel().isOpen())
		{
			e.getChannel().close();
		}
	}
	
	public void processProxyRequest()
	{
		try
		{
			fetch(true);
		}
		catch(Exception e)
		{
			logger.error("Encounter error.", e);
			if(channel.isConnected())
			{
				channel.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT)).addListener(ChannelFutureListener.CLOSE);
			}
		}
		
	}

	@Override
	public void callBack(RpcCallbackResult<HttpResponseExchange> result)
	{
		try
		{
			HttpResponseExchange forwardResponse = result.get();
			if(logger.isDebugEnabled())
			{
				logger.debug("Recv proxy response");
				logger.debug(forwardResponse.toPrintableString());
			}
			int fetchSizeLimit = Config.getInstance().getFetchLimitSize();
			if(forwardResponse.isResponseTooLarge())
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Try to start range fetch!");
				}
				if(!forwardRequest.containsHeader(HttpHeaders.Names.RANGE))
				{
					forwardRequest.setHeader(HttpHeaders.Names.RANGE, new RangeHeaderValue(0, fetchSizeLimit-1));
				}
				else
				{
					String hv = forwardRequest.getHeaderValue(HttpHeaders.Names.RANGE);
					RangeHeaderValue containedRange = new RangeHeaderValue(hv);
					forwardRequest.setHeader(HttpHeaders.Names.RANGE, new RangeHeaderValue(containedRange.getFirstBytePos(), containedRange.getFirstBytePos() + fetchSizeLimit-1));
				}
				forwardResponse = fetch();
			}
			
			if(null != lastContentRange)
			{
				forwardResponse = new RangeHttpProxyChunkedOutput().execute();
			}
			
			if(channel.isConnected())
			{	
				//forwardResponse.printMessage();
				String contentRangeValue =  forwardResponse.getHeaderValue(HttpHeaders.Names.CONTENT_RANGE);
				ContentRangeHeaderValue contentRange = null;
				if(null != contentRangeValue)
				{
					contentRange = new ContentRangeHeaderValue(contentRangeValue);
					forwardResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(contentRange.getInstanceLength()));
					forwardResponse.setResponseCode(200);
					if(!originalRequest.containsHeader(HttpHeaders.Names.RANGE))
					{	
						forwardResponse.removeHeader(HttpHeaders.Names.CONTENT_RANGE);
						forwardResponse.removeHeader(HttpHeaders.Names.ACCEPT_RANGES);
					}
					else
					{
						String originalRangeValue = originalRequest.getHeaderValue(HttpHeaders.Names.RANGE);
						RangeHeaderValue originalRange  = new RangeHeaderValue(originalRangeValue);
						forwardResponse.removeHeader(HttpHeaders.Names.CONTENT_RANGE);
						ContentRangeHeaderValue returnContentRange = new ContentRangeHeaderValue(contentRange.toString());
						if(originalRange.getLastBytePos()  > 0)
						{
							returnContentRange.setLastBytePos(originalRange.getLastBytePos());
						}
						else
						{
							returnContentRange.setLastBytePos(contentRange.getInstanceLength() - 1);
						}		
						forwardResponse.setHeader(HttpHeaders.Names.CONTENT_RANGE, returnContentRange);
					}
					
				}
				
				if(forwardResponse.getResponseCode() == 0)
				{
					forwardResponse.setResponseCode(400);
				}
				HttpResponse response = ClientUtils.buildHttpServletResponse(forwardResponse);
				//boolean close =  !(HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) || HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader("Proxy-Connection")));
				
				if(logger.isDebugEnabled())
				{
					logger.debug(" Received response for " + request.getMethod() + " " + request.getUri());
				}
				ChannelFuture future = channel.write(response);
				//future.await();
				if(null != contentRange && contentRange.getLastBytePos() < (contentRange.getInstanceLength() - 1))
				{
					chunkedInput = new RangeHttpProxyChunkedInput(fetchServiceSelector, workerExecutor, forwardRequest, contentRange.getLastBytePos() + 1, contentRange.getInstanceLength());
					future = channel.write(chunkedInput);
				}
				{
					future.addListener(ChannelFutureListener.CLOSE);
					final HttpResponseExchange res = forwardResponse;
					future.addListener(new ChannelFutureListener()
					{
						@Override
						public void operationComplete(ChannelFuture future) throws Exception
						{
							res.getBody().free();
						}
					});
				}
			}
			else
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Warn:Browser connection is already closed by browser.");
				}	
			}
		}
		catch(Throwable e)
		{
			logger.error("Encounter error for request:" + forwardRequest.url, e);
			if(channel.isConnected())
			{
				channel.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT)).addListener(ChannelFutureListener.CLOSE);
			}
		}
	}
	
	class RangeHttpProxyChunkedOutput
	{
		public HttpResponseExchange execute() throws Exception
		{
			HttpResponseExchange forwardResponse = null;
			int fetchSizeLimit = Config.getInstance().getFetchLimitSize();
			while(null != lastContentRange)
			{
				forwardRequest.setBody((ByteArray)null);
				ContentRangeHeaderValue old = lastContentRange;
				long sendSize = fetchSizeLimit;
				if(old.getInstanceLength() - old.getLastBytePos() - 1 < fetchSizeLimit)
				{
					sendSize = (old.getInstanceLength() - old.getLastBytePos() - 1);
				}
				if(sendSize <= 0)
				{
					break;
				}
				lastContentRange = new ContentRangeHeaderValue(old.getLastBytePos() + 1, old.getLastBytePos()  + sendSize, old.getInstanceLength());
				forwardRequest.setHeader(HttpHeaders.Names.CONTENT_RANGE, lastContentRange);
				forwardRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(sendSize));
				forwardResponse = fetch();
				if(sendSize < fetchSizeLimit)
				{
					lastContentRange = null;
				}
			}
			return forwardResponse;
		}
	}
}
