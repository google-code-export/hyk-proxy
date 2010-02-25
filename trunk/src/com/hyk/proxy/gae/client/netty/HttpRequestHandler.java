/**
 * 
 */
package com.hyk.proxy.gae.client.netty;

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

import org.apache.tools.ant.taskdefs.Sleep;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
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
import com.hyk.proxy.gae.client.util.ClientUtils;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.proxy.gae.common.http.ContentRangeHeaderValue;
import com.hyk.proxy.gae.common.http.RangeHeaderValue;
import com.hyk.proxy.gae.common.service.FetchService;

/**
 * @author Administrator
 * 
 */
@ChannelPipelineCoverage("one")
public class HttpRequestHandler extends SimpleChannelUpstreamHandler implements Runnable
{
	protected Logger				logger			= LoggerFactory.getLogger(getClass());

	private SSLContext				sslContext;
	private volatile HttpRequest	request;
	private volatile boolean		readingChunks;
	private final StringBuilder		responseContent	= new StringBuilder();
	private ChannelPipeline			channelPipeline;

	private boolean					ishttps			= false;
	private String					httpspath		= null;

	//private List<FetchService>		fetchServices;
	//private int						cursor;
	private FetchServiceSelector fetchServiceSelector;
	private Executor				workerExecutor;
	private HttpServer				httpServer;
	
	private HttpRequestExchange forwardRequest;
	private HttpResponseExchange forwardResponse;
	
	private ContentRangeHeaderValue lastContentRange = null;
	private ChannelBuffer leftChannelBuffer;
	private BlockingQueue<ChannelBuffer> proxyRequestBody = new LinkedBlockingQueue<ChannelBuffer>();

	private Channel	channel;
	private ChunkedInput chunkedInput;

	public HttpRequestHandler(SSLContext sslContext, ChannelPipeline channelPipeline, List<FetchService> fetchServices, Executor workerExecutor,
			HttpServer httpServer)
	{
		this.sslContext = sslContext;
		this.channelPipeline = channelPipeline;
		this.fetchServiceSelector = new FetchServiceSelector(fetchServices);
		this.workerExecutor = workerExecutor;
		this.httpServer = httpServer;
	}

	protected void fetch() throws InterruptedException
	{
		waitForwardBodyComplete();
		if(logger.isDebugEnabled())
		{
			logger.debug("Send proxy request");
			logger.debug(forwardRequest.toPrintableString());
		}
		forwardResponse = fetchServiceSelector.select().fetch(forwardRequest);
		if(logger.isDebugEnabled())
		{
			logger.debug("Recv proxy response");
			logger.debug(forwardResponse.toPrintableString());
		}
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
		
		if(recvReq.getContentLength() > fetchLimit)
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

		int bodyLen = gaeRequest.getContentLength();

		if(bodyLen > 0)
		{
			byte[] payload = new byte[bodyLen];
			ChannelBuffer body = recvReq.getContent();
			body.readBytes(payload);
			gaeRequest.setBody(payload);
		}
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
				e.getChannel().write(response).await();
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
				}
				return;
			}
			if(request.isChunked())
			{
				readingChunks = true;
			}
			this.forwardRequest = buildForwardRequest(request);
			this.channel = e.getChannel();
			workerExecutor.execute(this);
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

	private void writeResponse(MessageEvent e)
	{
		// Convert the response content to a ChannelBuffer.
		ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent.toString(), "UTF-8");
		responseContent.setLength(0);

		// Decide whether to close the connection or not.
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION))
				|| request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
				&& !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.setContent(buf);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

		if(!close)
		{
			// There's no need to add 'Content-Length' header
			// if this is the last response.
			response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
		}

		String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
		if(cookieString != null)
		{
			CookieDecoder cookieDecoder = new CookieDecoder();
			Set<Cookie> cookies = cookieDecoder.decode(cookieString);
			if(!cookies.isEmpty())
			{
				// Reset the cookies if necessary.
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				for(Cookie cookie : cookies)
				{
					cookieEncoder.addCookie(cookie);
				}
				response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
			}
		}

		// Write the response.
		ChannelFuture future = e.getChannel().write(response);

		// Close the connection after the write operation is done if necessary.
		if(close)
		{
			future.addListener(ChannelFutureListener.CLOSE);
		}
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

	
	
	@Override
	public void run()
	{
		long startTime = System.currentTimeMillis();
		try
		{
			HttpRequestExchange originalRequest = forwardRequest.clone();
			fetch();
			if(null == forwardResponse)
			{
				return;
			}
			int fetchSizeLimit = Config.getInstance().getFetchLimitSize();
			RangeHeaderValue containedRange = new RangeHeaderValue(fetchSizeLimit, -1);
			if(forwardResponse.isResponseTooLarge())
			{
				if(logger.isInfoEnabled())
				{
					logger.info("Start range fetch!");
				}
				if(!forwardRequest.containsHeader(HttpHeaders.Names.RANGE))
				{
					forwardRequest.setHeader(HttpHeaders.Names.RANGE, new RangeHeaderValue(0, fetchSizeLimit-1));
				}
				else
				{
					String hv = forwardRequest.getHeaderValue(HttpHeaders.Names.RANGE);
					containedRange = new RangeHeaderValue(hv);
					forwardRequest.setHeader(HttpHeaders.Names.RANGE, new RangeHeaderValue(containedRange.getFirstBytePos(), containedRange.getFirstBytePos() + fetchSizeLimit-1));
				}
				
				//forwardResponse = selectFetchService().fetch(forwardRequest);
				fetch();
			}

			while(null != lastContentRange)
			{
				forwardRequest.setBody(null);
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
				fetch();
				if(sendSize < fetchSizeLimit)
				{
					lastContentRange = null;
				}
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
				HttpResponse response = ClientUtils.buildHttpServletResponse(forwardResponse);
				boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION))
						|| request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
						&& !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));
				if(logger.isDebugEnabled())
				{
					logger.debug(" Received response for " + request.getMethod() + " " + request.getUri());
				}
				ChannelFuture future = channel.write(response);
				future.await();
				if(null != contentRange && contentRange.getLastBytePos() < (contentRange.getInstanceLength() - 1))
				{
					chunkedInput = new RangeHttpProxyChunkedInput(fetchServiceSelector, forwardRequest, contentRange.getLastBytePos() + 1, contentRange.getInstanceLength());
					future = channel.write(chunkedInput);
				}
				if(close)
				{
					future.addListener(ChannelFutureListener.CLOSE);
				}
			}
			else
			{
				long current = System.currentTimeMillis();
				if(logger.isInfoEnabled())
				{
					logger.info("Warn:Browser connection is already closed by browser. It wait " + (current - startTime) + "ms");
				}	
			}
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
}
