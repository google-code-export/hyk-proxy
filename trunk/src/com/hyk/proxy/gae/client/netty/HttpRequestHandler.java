/**
 * 
 */
package com.hyk.proxy.gae.client.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

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

	private List<FetchService>		fetchServices;
	private int						cursor;
	private Executor				workerExecutor;
	private HttpServer				httpServer;
	
	private HttpRequestExchange forwardRequest;

	private Channel	channel;

	public HttpRequestHandler(SSLContext sslContext, ChannelPipeline channelPipeline, List<FetchService> fetchServices, Executor workerExecutor,
			HttpServer httpServer)
	{
		this.sslContext = sslContext;
		this.channelPipeline = channelPipeline;
		this.fetchServices = fetchServices;
		this.workerExecutor = workerExecutor;
		this.httpServer = httpServer;
	}

	protected synchronized FetchService selectFetchService()
	{
		if(cursor >= fetchServices.size())
		{
			cursor = 0;
		}
		return fetchServices.get(cursor++);
	}

	protected HttpRequestExchange buildForwardRequest(HttpRequest request) throws IOException
	{
		HttpRequestExchange gaeRequest = new HttpRequestExchange();
		StringBuffer urlbuffer = new StringBuffer();
		if(ishttps)
		{
			urlbuffer.append("https://").append(httpspath);
		}
		urlbuffer.append(request.getUri());
		gaeRequest.setURL(urlbuffer.toString());
		gaeRequest.setMethod(request.getMethod().getName());
		Set<String> headers = request.getHeaderNames();
		for(String headerName : headers)
		{
			List<String> headerValues = request.getHeaders(headerName);
			if(null != headerValues)
			{
				for(String headerValue : headerValues)
				{
					gaeRequest.addHeader(headerName, headerValue);
				}
			}
		}
		
		gaeRequest.setHeader(HttpHeaders.Names.RANGE, new RangeHeaderValue(0, 640000-1));

		int bodyLen = (int)request.getContentLength();

		if(bodyLen > 0)
		{
			byte[] payload = new byte[bodyLen];
			ChannelBuffer body = request.getContent();
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
			final HttpRequest request = this.request = (HttpRequest)e.getMessage();
			if(logger.isDebugEnabled())
			{
				logger.debug(request.getMethod() + " " + request.getUri());
			}
			if(request.getMethod().equals(HttpMethod.CONNECT))
			{
				ishttps = true;
				httpspath = request.getHeader("Host");
				HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
				e.getChannel().write(response).await(10000);
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
				responseContent.append("END OF CONTENT\r\n");
				writeResponse(e);
			}
			else
			{
				responseContent.append("CHUNK: " + chunk.getContent().toString("UTF-8") + "\r\n");
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
		e.getChannel().close();
	}

	
	
	@Override
	public void run()
	{
		long startTime = System.currentTimeMillis();
		FetchService fetchService =  selectFetchService();
		try
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Send proxy request");
				logger.debug(forwardRequest.toPrintableString());
			}
			HttpResponseExchange forwardResponse = fetchService.fetch(forwardRequest);
			if(null == forwardResponse)
			{
				return;
			}
			if(logger.isDebugEnabled())
			{
				logger.debug("Recv proxy response");
				logger.debug(forwardResponse.toPrintableString());
			}
			if(channel.isConnected())
			{	
				//forwardResponse.printMessage();
				String contentRangeValue =  forwardResponse.getHeaderValue(HttpHeaders.Names.CONTENT_RANGE);
				ContentRangeHeaderValue contentRange = null;
				if(null != contentRangeValue)
				{
					contentRange = new ContentRangeHeaderValue(contentRangeValue);
					forwardResponse.removeHeader(HttpHeaders.Names.CONTENT_RANGE);
					forwardResponse.removeHeader(HttpHeaders.Names.ACCEPT_RANGES);
					forwardResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(contentRange.getInstanceLength()));
					forwardResponse.setResponseCode(200);
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
					future = channel.write(new RangeHttpProxyChunkedInput(fetchService, forwardRequest, contentRange.getInstanceLength()));
				}
				//if(close)
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
		}
		
	}
}
