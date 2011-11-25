/**
 * 
 */
package org.hyk.proxy.gae.client.handler;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;
import org.hyk.proxy.gae.client.connection.ClientConnection;
import org.hyk.proxy.gae.client.connection.http.HTTPConnectionManager;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 * 
 */
public class ProxySession implements Runnable
{
	private static Map<Integer, ProxySession> sessionTable = new HashMap<Integer, ProxySession>();
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private ClientConnection connection = null;

	private Channel localHTTPChannel;
	private boolean isHttps;
	private String httpspath;

	public ProxySession(Channel localChannel)
	{
		this.localHTTPChannel = localChannel;
	}

	private ClientConnection getClientConnection()
	{
		if (null == connection)
		{

		}
		return connection;
	}

	private void handleConnect(HTTPRequestEvent event)
	{
		isHttps = true;
		httpspath = event.getHeader("Host");
		if (httpspath == null)
		{
			httpspath = event.url;
		}
		String httpshost = httpspath;
		String httpsport = "443";
		if (httpspath.indexOf(":") != -1)
		{
			httpshost = httpspath.substring(0, httpspath.indexOf(":"));
			httpsport = httpspath.substring(httpspath.indexOf(":") + 1);
		}
		HttpResponse OK = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
		        HttpResponseStatus.OK);
		localHTTPChannel.write(OK).addListener(new ChannelFutureListener()
		{
			@Override
			public void operationComplete(ChannelFuture future)
			        throws Exception
			{
				if (localHTTPChannel.getPipeline().get("ssl") == null)
				{
					InetSocketAddress remote = (InetSocketAddress) localHTTPChannel
					        .getRemoteAddress();
					SSLContext sslContext = null;
					SSLEngine engine = sslContext.createSSLEngine(remote
					        .getAddress().getHostAddress(), remote.getPort());
					engine.setUseClientMode(false);
					localHTTPChannel.getPipeline().addBefore("decoder", "ssl",
					        new SslHandler(engine));
				}

			}
		});
	}

	private void adjustEvent(HTTPRequestEvent event)
	{
		StringBuffer urlbuffer = new StringBuffer();
		if (isHttps)
		{
			urlbuffer.append("https://").append(httpspath);
		}
		else
		{
			if (!event.url.toLowerCase().startsWith("http://"))
			{
				urlbuffer.append("http://").append(
				        event.getHeader(HttpHeaders.Names.HOST));
			}
		}
		urlbuffer.append(event.url);
		event.url = urlbuffer.toString();

		int fetchLimit = 200000;

		// if(recvReq.getContentLength() > fetchLimit)
		if (recvReq.getContentLength() > Constants.APPENGINE_HTTP_BODY_LIMIT)
		{
			ContentRangeHeaderValue contentRange = new ContentRangeHeaderValue(
			        0, fetchLimit - 1, recvReq.getContentLength());
			gaeRequest.setHeader(HttpHeaders.Names.CONTENT_RANGE, contentRange);
			gaeRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
			        String.valueOf(fetchLimit));
		}
		ChannelBuffer contentBody = recvReq.getContent();
		if (null != contentBody)
		{
			chunkedBodys.add(contentBody);
		}
		originalRequest = gaeRequest.clone();
		// Try to
		if (!containRangeHeader
		        && Config.getInstance().isInjectRangeHeaderSitesMatchHost(
		                recvReq.getHeader(HttpHeaders.Names.HOST)))
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Inject a range header for host:"
				        + recvReq.getHeader(HttpHeaders.Names.HOST));
			}
			// logger.info("Inject a range header for host:" + host);
			int fetchSizeLimit = Config.getInstance().getFetchLimitSize();
			gaeRequest.setHeader(HttpHeaders.Names.RANGE, new RangeHeaderValue(
			        0, fetchSizeLimit - 1));
		}
	}

	public void handle(HTTPRequestEvent event)
	{
		if (event.method.equalsIgnoreCase(HttpMethod.CONNECT.getName()))
		{
			handleConnect(event);
		}
		else
		{
			adjustEvent(event);
			getClientConnection().send(event);
		}
	}

	public void handle(HTTPChunkEvent event)
	{

	}

	@Override
	public void run()
	{

	}
}
