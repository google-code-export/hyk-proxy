/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyService.java 
 *
 * @author yinqiwen [ 2010-5-13 | ÏÂÎç07:50:44 ]
 *
 */
package com.hyk.proxy.client.gae.event;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
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
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.gae.event.GoogleAppEngineHttpProxyEventServiceFactory.FetchServiceSelector;
import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.client.framework.event.HttpProxyEvent;
import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.common.rpc.service.AsyncFetchService;
import com.hyk.proxy.common.rpc.service.FetchService;
import com.hyk.proxy.client.config.Config;
//import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.common.http.header.ContentRangeHeaderValue;
import com.hyk.proxy.common.http.header.RangeHeaderValue;
import com.hyk.proxy.common.http.message.HttpRequestExchange;
import com.hyk.proxy.common.http.message.HttpResponseExchange;
import com.hyk.rpc.core.RpcCallback;
import com.hyk.rpc.core.RpcCallbackResult;
import com.hyk.util.buffer.ByteArray;

/**
 *
 */
class GoogleAppEngineHttpProxyEventService implements HttpProxyEventService, RpcCallback<HttpResponseExchange>
{
	protected Logger					logger		= LoggerFactory.getLogger(getClass());

	private FetchServiceSelector		selector;
	private SSLContext					sslContext;
	private boolean						ishttps;
	private String						httpspath;
	private Channel						channel;
	private List<ChannelBuffer>			chunkedBody	= new LinkedList<ChannelBuffer>();
	private HttpRequestExchange			originalRequest;
	private HttpRequestExchange			forwardRequest;

	private RangeHttpProxyChunkedInput	chunkedInput;

	private Executor	workerExecutor;

	GoogleAppEngineHttpProxyEventService(FetchServiceSelector selector, SSLContext sslContext, Executor	workerExecutor)
	{
		this.selector = selector;
		this.sslContext = sslContext;
		this.workerExecutor = workerExecutor;
	}

	protected HttpRequestExchange buildForwardRequest(HttpRequest recvReq)
	{
		HttpRequestExchange gaeRequest = new HttpRequestExchange();
		StringBuffer urlbuffer = new StringBuffer();
		if(ishttps)
		{
			urlbuffer.append("https://").append(httpspath);
		}
		else
		{
			if(!recvReq.getUri().toLowerCase().startsWith("http://"))
			{
				urlbuffer.append("http://").append(recvReq.getHeader(HttpHeaders.Names.HOST));
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

		int fetchLimit = 200000;

		// if(recvReq.getContentLength() > fetchLimit)
		if(recvReq.getContentLength() > 1024000) // GAE's limit 1M
		{
			ContentRangeHeaderValue contentRange = new ContentRangeHeaderValue(0, fetchLimit - 1, recvReq.getContentLength());
			gaeRequest.setHeader(HttpHeaders.Names.CONTENT_RANGE, contentRange);
			gaeRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(fetchLimit));
		}
		ChannelBuffer contentBody = recvReq.getContent();
		if(null != contentBody)
		{
			chunkedBody.add(contentBody);
		}
		return gaeRequest;
	}

	@Override
	public void handleEvent(HttpProxyEvent event)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Handle event:" + event.getType());
		}
		switch(event.getType())
		{
			case RECV_HTTP_REQUEST:
			case RECV_HTTPS_REQUEST:
			{
				this.channel = event.getChannel();
				HttpRequest request = (HttpRequest)event.getSource();
				if(request.getMethod().equals(HttpMethod.CONNECT))
				{
					ishttps = true;
					httpspath = request.getHeader("Host");
					HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
					event.getChannel().write(response);
					// https connection
					if(event.getChannel().getPipeline().get("ssl") == null)
					{
						InetSocketAddress remote = (InetSocketAddress)event.getChannel().getRemoteAddress();
						SSLEngine engine = sslContext.createSSLEngine(remote.getAddress().getHostAddress(), remote.getPort());
						engine.setUseClientMode(false);
						event.getChannel().getPipeline().addBefore("decoder", "ssl", new SslHandler(engine));
					}
				}
				else
				{
					forwardRequest = buildForwardRequest(request);
					originalRequest = forwardRequest.clone();
					asyncFetch(forwardRequest);
				}
				break;
			}
			case RECV_HTTP_CHUNK:
			case RECV_HTTPS_CHUNK:
			{

				break;
			}
		}
	}

	protected void asyncFetch(HttpRequestExchange req)
	{
		AsyncFetchService fetchService = selector.selectAsync();
		fetchService.fetch(req, this);
		if(logger.isDebugEnabled())
		{
			logger.debug("Send proxy request");
			logger.debug(req.toPrintableString());
		}
	}

	protected HttpResponseExchange syncFetch(HttpRequestExchange req)
	{
		FetchService fetchService = selector.select();
		if(logger.isDebugEnabled())
		{
			logger.debug("Send proxy request");
			logger.debug(req.toPrintableString());
		}
		HttpResponseExchange res = fetchService.fetch(req);
		if(logger.isDebugEnabled())
		{
			logger.debug("Recv proxy response");
			logger.debug(res.toPrintableString());
		}
		return res;
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
					forwardRequest.setHeader(HttpHeaders.Names.RANGE, new RangeHeaderValue(0, fetchSizeLimit - 1));
				}
				else
				{
					String hv = forwardRequest.getHeaderValue(HttpHeaders.Names.RANGE);
					RangeHeaderValue containedRange = new RangeHeaderValue(hv);
					forwardRequest.setHeader(HttpHeaders.Names.RANGE, new RangeHeaderValue(containedRange.getFirstBytePos(),
							containedRange.getFirstBytePos() + fetchSizeLimit - 1));
				}
				forwardResponse = syncFetch(forwardRequest);
			}

			//Proxy request with Content-Range Header
			if(forwardRequest.containsHeader(HttpHeaders.Names.CONTENT_RANGE))
			{
				ContentRangeHeaderValue lastContentRange = new ContentRangeHeaderValue(forwardRequest.getHeaderValue(HttpHeaders.Names.CONTENT_RANGE));
				forwardResponse = new RangeHttpProxyChunkedOutput(lastContentRange).execute();
			}

			if(channel.isConnected())
			{
				String contentRangeValue = forwardResponse.getHeaderValue(HttpHeaders.Names.CONTENT_RANGE);
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
						RangeHeaderValue originalRange = new RangeHeaderValue(originalRangeValue);
						forwardResponse.removeHeader(HttpHeaders.Names.CONTENT_RANGE);
						ContentRangeHeaderValue returnContentRange = new ContentRangeHeaderValue(contentRange.toString());
						if(originalRange.getLastBytePos() > 0)
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
				// if(logger.isDebugEnabled())
				// {
				// logger.debug(" Received response for " + request.getMethod()
				// + " " + request.getUri());
				// }
				ChannelFuture future = channel.write(response);
				// future.await();
				if(null != contentRange && contentRange.getLastBytePos() < (contentRange.getInstanceLength() - 1))
				{
					chunkedInput = new RangeHttpProxyChunkedInput(selector, workerExecutor, forwardRequest, contentRange.getLastBytePos() + 1,
							contentRange.getInstanceLength());
					future = channel.write(chunkedInput);
				}
				future.addListener(ChannelFutureListener.CLOSE);
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
				channel.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT)).addListener(
						ChannelFutureListener.CLOSE);
			}
		}
	}

	class RangeHttpProxyChunkedOutput
	{
		private ContentRangeHeaderValue	lastContentRange;

		public RangeHttpProxyChunkedOutput(ContentRangeHeaderValue lastContentRange)
		{
			this.lastContentRange = lastContentRange;
		}

		public HttpResponseExchange execute() throws Exception
		{
			HttpResponseExchange forwardResponse = null;
			int fetchSizeLimit = Config.getInstance().getFetchLimitSize();
			while(null != lastContentRange)
			{
				forwardRequest.setBody(new byte[0]);
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
				lastContentRange = new ContentRangeHeaderValue(old.getLastBytePos() + 1, old.getLastBytePos() + sendSize, old.getInstanceLength());
				forwardRequest.setHeader(HttpHeaders.Names.CONTENT_RANGE, lastContentRange);
				forwardRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(sendSize));
				forwardResponse = syncFetch(forwardRequest);
				if(sendSize < fetchSizeLimit)
				{
					lastContentRange = null;
				}
			}
			return forwardResponse;
		}
	}
}
