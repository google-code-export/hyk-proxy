/**
 * 
 */
package org.hyk.proxy.gae.client.handler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.arch.buffer.Buffer;
import org.arch.common.KeyValuePair;
import org.arch.event.Event;
import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPErrorEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;
import org.hyk.proxy.core.util.SslCertificateHelper;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration;
import org.hyk.proxy.gae.client.connection.ProxyConnection;
import org.hyk.proxy.gae.client.connection.ProxyConnectionManager;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.http.ContentRangeHeaderValue;
import org.hyk.proxy.gae.common.http.RangeHeaderValue;
import org.hyk.proxy.gae.common.http.SetCookieHeaderValue;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
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
public class ProxySession
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private ProxyConnectionManager connectionManager = ProxyConnectionManager
	        .getInstance();
	private ProxyConnection connection = null;

	private Integer sessionID;
	private Channel localHTTPChannel;
	private HTTPRequestEvent proxyEvent;
	private boolean isHttps;
	private String httpspath;
	private ProxySessionStatus status;

	private Map<Long,Buffer> rangeFetchContents = new HashMap<Long, Buffer>();
	private long waitingWriteStreamPos = -1;

	public ProxySession(Integer id, Channel localChannel)
	{
		this.sessionID = id;
		this.localHTTPChannel = localChannel;
	}

	private HTTPRequestEvent cloneHeaders(HTTPRequestEvent event)
	{
		HTTPRequestEvent newEvent = new HTTPRequestEvent();
		newEvent.headers = new ArrayList<KeyValuePair<String, String>>();
		newEvent.headers.addAll(event.getHeaders());
		return newEvent;
	}

	public Integer getSessionID()
	{
		return sessionID;
	}

	private ProxyConnection getClientConnection(HTTPRequestEvent event)
	{
		if (null == connection)
		{
			connection = connectionManager.getClientConnection(event);
		}
		return connection;
	}

	private ProxyConnection getConcurrentClientConnection(HTTPRequestEvent event)
	{
		return connectionManager.getClientConnection(event);
	}

	private boolean rangeFetch(RangeHeaderValue range,
	        ContentRangeHeaderValue contentRange, int wokerNum)
	{
		int fetchSizeLimit = GAEClientConfiguration.getInstance()
		        .getFetchLimitSize();
		int concurrentWorkerNum = wokerNum < 0 ? GAEClientConfiguration
		        .getInstance().getConcurrentRangeFetchWorker() : wokerNum;
		status = ProxySessionStatus.WAITING_MULTI_RANGE_RESPONSE;
		long start = contentRange.getLastBytePos();
		long limit = 0;
		if (null != range)
		{
			limit = range.getLastBytePos();
		}
		else
		{
			limit = contentRange.getInstanceLength() - 1;
		}
		for (int i = 0; i < concurrentWorkerNum; i++)
		{
			long begin = start + 1;
			if (begin >= limit)
			{
				break;
			}
			long end = start + fetchSizeLimit;
			if (end > limit)
			{
				end = limit;
			}
			start = end;
			RangeHeaderValue headerValue = new RangeHeaderValue(begin, end);
			HTTPRequestEvent newEvent = cloneHeaders(proxyEvent);
			newEvent.setHeader(HttpHeaders.Names.RANGE, headerValue.toString());
			getConcurrentClientConnection(newEvent).send(newEvent);
			if(-1 == waitingWriteStreamPos)
			{
				waitingWriteStreamPos = begin;
			}
		}
		if(waitingWriteStreamPos >= limit)
		{
			waitingWriteStreamPos = -1;
			status = ProxySessionStatus.SESSION_COMPLETED;
			rangeFetchContents.clear();
		}
		return true;
	}

	private void handleMultiRangeFetchResponse(HTTPResponseEvent ev)
	{
		String contentRangeValue = ev
		        .getHeader(HttpHeaders.Names.CONTENT_RANGE);
		ContentRangeHeaderValue contentRange = new ContentRangeHeaderValue(
		        contentRangeValue);
		String rangeValue = proxyEvent.getHeader(HttpHeaders.Names.RANGE);
		RangeHeaderValue range = new RangeHeaderValue(rangeValue);
		rangeFetchContents.put(contentRange.getFirstBytePos(), ev.content);
		while(rangeFetchContents.containsKey(waitingWriteStreamPos))
		{
			Buffer content = rangeFetchContents.remove(waitingWriteStreamPos);
			ChannelBuffer buf = ChannelBuffers.wrappedBuffer(content.getRawBuffer(), content.getReadIndex(), content.readableBytes());
			localHTTPChannel.write(buf);
			waitingWriteStreamPos = contentRange.getLastBytePos() + 1;
		}
		rangeFetch(range, contentRange, 1);
	}

	private HttpResponse buildHttpResponse(HTTPResponseEvent ev)
	{
		if (null == ev)
		{
			return new DefaultHttpResponse(HttpVersion.HTTP_1_1,
			        HttpResponseStatus.REQUEST_TIMEOUT);
		}
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
		        HttpResponseStatus.valueOf(ev.statusCode));

		List<KeyValuePair<String, String>> headers = ev.getHeaders();
		for (KeyValuePair<String, String> header : headers)
		{
			if (header.getName().equalsIgnoreCase(HttpHeaders.Names.SET_COOKIE)
			        || header.getName().equalsIgnoreCase(
			                HttpHeaders.Names.SET_COOKIE2))
			{
				List<SetCookieHeaderValue> cookies = SetCookieHeaderValue
				        .parse(header.getValue());
				for (SetCookieHeaderValue cookie : cookies)
				{
					response.addHeader(header.getName(), cookie.toString());
				}
			}
			else
			{
				response.addHeader(header.getName(), header.getValue());
			}
		}

		String contentRangeValue = ev
		        .getHeader(HttpHeaders.Names.CONTENT_RANGE);
		if (null != contentRangeValue)
		{
			ContentRangeHeaderValue contentRange = new ContentRangeHeaderValue(
			        contentRangeValue);
			if (!proxyEvent.containsHeader(HttpHeaders.Names.RANGE))
			{
				response.removeHeader(HttpHeaders.Names.CONTENT_RANGE);
				response.removeHeader(HttpHeaders.Names.ACCEPT_RANGES);
				response.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
				        String.valueOf(contentRange.getInstanceLength()));
				response.setStatus(HttpResponseStatus.OK);
			}
			else
			{
				String rangeValue = proxyEvent
				        .getHeader(HttpHeaders.Names.RANGE);
				RangeHeaderValue range = new RangeHeaderValue(rangeValue);
				if (range.getLastBytePos() > 0)
				{
					contentRange.setLastBytePos(range.getLastBytePos());
				}
				else
				{
					contentRange.setLastBytePos(contentRange
					        .getInstanceLength() - 1);
				}
				response.setHeader(HttpHeaders.Names.CONTENT_RANGE,
				        contentRange);
				response.setHeader(
				        HttpHeaders.Names.CONTENT_LENGTH,
				        (contentRange.getLastBytePos()
				                - contentRange.getFirstBytePos() + 1));
			}
		}

		if (ev.content.readable())
		{
			ChannelBuffer bufer = ChannelBuffers.wrappedBuffer(
			        ev.content.getRawBuffer(), ev.content.getReadIndex(),
			        ev.content.readableBytes());
			response.setContent(bufer);
		}
		return response;
	}

	private void handleNormalFetchResponse(HTTPResponseEvent ev)
	{
		ContentRangeHeaderValue contentRange = null;
		String contentRangeStr = ev.getHeader(HttpHeaders.Names.CONTENT_RANGE);
		if (null != contentRangeStr)
		{
			contentRange = new ContentRangeHeaderValue(contentRangeStr);
		}
		HttpResponse response = buildHttpResponse(ev);
		ChannelFuture future = localHTTPChannel.write(response);
		// future.await();
		if (null != contentRange
		        && contentRange.getLastBytePos() < (contentRange
		                .getInstanceLength() - 1))
		{
			String rangeHeaderValue = proxyEvent
			        .getHeader(HttpHeaders.Names.RANGE);
			RangeHeaderValue range = null;
			if (rangeHeaderValue != null)
			{
				range = new RangeHeaderValue(rangeHeaderValue);
				if (range.getLastBytePos() >= contentRange.getLastBytePos())
				{
					status = ProxySessionStatus.SESSION_COMPLETED;
					return;
				}
			}
			status = ProxySessionStatus.WAITING_MULTI_RANGE_RESPONSE;
			rangeFetch(range, contentRange, -1);
		}
		else
		{
			status = ProxySessionStatus.SESSION_COMPLETED;
		}
	}

	public void handleResponse(Event res)
	{
		if (res instanceof HTTPResponseEvent)
		{
			switch (status)
			{
				case WAITING_NORMAL_RESPONSE:
				{
					handleNormalFetchResponse((HTTPResponseEvent) res);
					break;
				}
				case WAITING_MULTI_RANGE_RESPONSE:
				{
					handleMultiRangeFetchResponse((HTTPResponseEvent) res);
					break;
				}
				default:
				{
					break;
				}
			}
		}
		else if (res instanceof HTTPErrorEvent)
		{
			HTTPErrorEvent error = (HTTPErrorEvent) res;
			if (error.errno == GAEConstants.FETCH_FAILED)
			{
				// retryRangeFetch();
				close();
			}
		}
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
		final SSLContext sslContext;
		try
		{
			sslContext = SslCertificateHelper.getFakeSSLContext(httpshost,
			        httpsport);
		}
		catch (Exception e)
		{
			localHTTPChannel.close();
			logger.error("Failed to get SSL fake cert for " + httpshost + ":"
			        + httpsport, e);
			return;
		}
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

		proxyEvent = event;

		if (proxyEvent.getContentLength() > GAEConstants.APPENGINE_HTTP_BODY_LIMIT)
		{
			ContentRangeHeaderValue contentRange = new ContentRangeHeaderValue(
			        0, proxyEvent.getCurrentContentLength() - 1,
			        event.getContentLength());
			proxyEvent.setHeader(HttpHeaders.Names.CONTENT_RANGE,
			        String.valueOf(contentRange));
			proxyEvent.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
			        String.valueOf(proxyEvent.getCurrentContentLength()));
		}
		else
		{
			if (GAEClientConfiguration.getInstance()
			        .isInjectRangeHeaderSitesMatchHost(
			                proxyEvent.getHeader(HttpHeaders.Names.HOST)))
			{
				int fetchSizeLimit = GAEClientConfiguration.getInstance()
				        .getFetchLimitSize();
				if (logger.isDebugEnabled())
				{
					logger.debug("Inject a range header for host:"
					        + proxyEvent.getHeader(HttpHeaders.Names.HOST));
				}
				// logger.info("Inject a range header for host:" + host);
				proxyEvent.setHeader(HttpHeaders.Names.RANGE,
				        new RangeHeaderValue(0, fetchSizeLimit - 1).toString());
			}
		}

	}

	protected boolean isProxyRequestReady(HTTPRequestEvent event)
	{
		int contentLength = event.getContentLength();
		if (contentLength > 0 && event.content.readableBytes() < contentLength)
		{
			return false;
		}
		return true;
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
			if (isProxyRequestReady(event))
			{
				getClientConnection(event).send(event);
				status = ProxySessionStatus.WAITING_NORMAL_RESPONSE;
			}
		}
	}

	private void completeProxyRequest(HTTPChunkEvent event)
	{
		proxyEvent.content.write(event.content);
		if (isProxyRequestReady(proxyEvent))
		{
			getClientConnection(proxyEvent).send(event);
			status = ProxySessionStatus.WAITING_NORMAL_RESPONSE;
		}

	}

	public void handle(HTTPChunkEvent event)
	{
		if (null != proxyEvent && !isProxyRequestReady(proxyEvent))
		{
			completeProxyRequest(event);
			return;
		}
		else
		{

		}
	}

	public void close()
	{
		waitingWriteStreamPos = -1;
		rangeFetchContents.clear();
		ProxySessionManager.getInstance().removeSession(this);
	}

}
