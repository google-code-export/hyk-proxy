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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.arch.buffer.Buffer;
import org.arch.common.KeyValuePair;
import org.arch.event.Event;
import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPErrorEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;
import org.hyk.proxy.core.util.SharedObjectHelper;
import org.hyk.proxy.core.util.SslCertificateHelper;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration;
import org.hyk.proxy.gae.client.connection.ProxyConnection;
import org.hyk.proxy.gae.client.connection.ProxyConnectionManager;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.GAEEventHelper;
import org.hyk.proxy.gae.common.http.ContentRangeHeaderValue;
import org.hyk.proxy.gae.common.http.RangeHeaderValue;
import org.hyk.proxy.gae.common.http.SetCookieHeaderValue;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 * 
 */
public class ProxySession
{
	protected static Logger logger = LoggerFactory
	        .getLogger(ProxySession.class);
	private ProxyConnectionManager connectionManager = ProxyConnectionManager
	        .getInstance();
	private ProxyConnection connection = null;

	private Integer sessionID;
	private Channel localHTTPChannel;
	private HTTPRequestEvent proxyEvent;
	private boolean isHttps;
	private String httpspath;
	private ProxySessionStatus status = ProxySessionStatus.INITED;

	private Map<Long, Buffer> rangeFetchContents = new HashMap<Long, Buffer>();
	private boolean isOriginalContainsRangeHeader = false;
	private long waitingWriteStreamPos = -1;
	private long waitingFetchStreamPos = 0;
	private Buffer uploadBuffer = new Buffer(0);

	private RangeChunkedInput rangeChunk = null;
	private Map<Long, ScheduledFuture> rangeRetryTaskTable;

	class RangeChunkedInput implements ChunkedInput
	{
		private LinkedList<ChannelBuffer> bufList = new LinkedList<ChannelBuffer>();

		@Override
		public boolean hasNextChunk() throws Exception
		{
			return waitingWriteStreamPos >= 0;
		}

		public void offer(ChannelBuffer buf)
		{
			synchronized (bufList)
			{
				bufList.addLast(buf);
				bufList.notify();
			}
		}

		@Override
		public Object nextChunk() throws Exception
		{
			synchronized (bufList)
			{
				if (bufList.isEmpty())
				{
					bufList.wait(60000);
				}
				if (bufList.isEmpty())
				{
					return ChannelBuffers.EMPTY_BUFFER;
				}
				return bufList.removeFirst();
			}
		}

		@Override
		public void close() throws Exception
		{
			synchronized (bufList)
			{
				bufList.notify();
			}
		}
	}

	public ProxySession(Integer id, Channel localChannel)
	{
		this.sessionID = id;
		this.localHTTPChannel = localChannel;
	}

	public ProxySessionStatus getStatus()
	{
		return status;
	}

	private RangeChunkedInput getRangeChunkedInput()
	{
		if (null == rangeChunk)
		{
			rangeChunk = new RangeChunkedInput();
		}
		return rangeChunk;
	}

	private void saveRangeRetryTask(ScheduledFuture future, RangeHeaderValue v)
	{
		if (null == rangeRetryTaskTable)
		{
			rangeRetryTaskTable = new ConcurrentHashMap<Long, ScheduledFuture>();
		}
		rangeRetryTaskTable.put(v.getFirstBytePos(), future);
	}

	private void cancelRangeRetryTask(ContentRangeHeaderValue v)
	{
		if (null != rangeRetryTaskTable)
		{
			ScheduledFuture future = rangeRetryTaskTable.remove(v
			        .getFirstBytePos());
			if (null != future)
			{
				future.cancel(true);
			}
		}
		if (null != rangeChunk)
		{
			try
            {
	            rangeChunk.close();
            }
            catch (Exception e)
            {
	            //
            }
		}
	}

	private void cancelAllRangeRetryTask()
	{
		if (null != rangeRetryTaskTable)
		{
			for (ScheduledFuture future : rangeRetryTaskTable.values())
			{
				future.cancel(true);
			}
		}
	}

	private HTTPRequestEvent cloneHeaders(HTTPRequestEvent event)
	{
		HTTPRequestEvent newEvent = new HTTPRequestEvent();
		newEvent.method = event.method;
		newEvent.url = event.url;
		newEvent.headers = new ArrayList<KeyValuePair<String, String>>();
		for (KeyValuePair<String, String> header : event.getHeaders())
		{
			newEvent.addHeader(header.getName(), header.getValue());
		}
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

	private boolean rangeFetch(RangeHeaderValue originRange, long limitSize,
	        int wokerNum)
	{
		int fetchSizeLimit = GAEClientConfiguration.getInstance()
		        .getFetchLimitSize();
		int concurrentWorkerNum = wokerNum < 0 ? GAEClientConfiguration
		        .getInstance().getConcurrentRangeFetchWorker() : wokerNum;
		status = ProxySessionStatus.WAITING_MULTI_RANGE_RESPONSE;
		long start = waitingFetchStreamPos;
		long limit = 0;
		if (null != originRange)
		{
			limit = originRange.getLastBytePos();
		}
		else
		{
			limit = limitSize - 1;
		}
		for (int i = 0; i < concurrentWorkerNum; i++)
		{
			if (waitingWriteStreamPos > 0
			        && waitingFetchStreamPos - waitingWriteStreamPos > 1024 * 1024)
			{
				break;
			}
			long begin = start;
			if (begin >= limit)
			{
				break;
			}
			long end = start + fetchSizeLimit - 1;
			if (end > limit)
			{
				end = limit;
			}
			start = end + 1;
			final RangeHeaderValue headerValue = new RangeHeaderValue(begin,
			        end);
			final HTTPRequestEvent newEvent = cloneHeaders(proxyEvent);
			newEvent.setAttachment(proxyEvent.getAttachment());
			newEvent.setHeader(HttpHeaders.Names.RANGE, headerValue.toString());
			Runnable retryTask = new Runnable()
			{
				@Override
				public void run()
				{
					if (logger.isDebugEnabled())
					{
						logger.debug("Retry range fetch since no response received in 10s, range fetch request is:"
						        + newEvent);
					}
					getConcurrentClientConnection(newEvent).send(newEvent);
				}
			};
			ScheduledFuture future = SharedObjectHelper.getGlobalTimer()
			        .scheduleAtFixedRate(retryTask, 10, 10, TimeUnit.SECONDS);
			saveRangeRetryTask(future, headerValue);
			SharedObjectHelper.getGlobalThreadPool().submit(new Runnable()
			{
				@Override
				public void run()
				{
					getConcurrentClientConnection(newEvent).send(newEvent);
				}
			});

			if (-1 == waitingWriteStreamPos)
			{
				waitingWriteStreamPos = begin;
			}
			waitingFetchStreamPos = start;

		}
		if (waitingWriteStreamPos >= limit)
		{
			waitingWriteStreamPos = -1;
			status = ProxySessionStatus.SESSION_COMPLETED;
			rangeFetchContents.clear();
		}
		return true;
	}

	private synchronized void handleMultiRangeFetchResponse(HTTPResponseEvent ev)
	{
		String contentRangeValue = ev
		        .getHeader(HttpHeaders.Names.CONTENT_RANGE);
		if (logger.isDebugEnabled())
		{
			logger.debug("Handle MultiRangeFetchResponse:" + ev.toString());
		}
		if (null == contentRangeValue)
		{
			logger.error("No content range header in response:" + ev);
			close(null);
			return;
		}
		ContentRangeHeaderValue contentRange = new ContentRangeHeaderValue(
		        contentRangeValue);
		cancelRangeRetryTask(contentRange);
		String rangeValue = proxyEvent.getHeader(HttpHeaders.Names.RANGE);
		RangeHeaderValue range = isOriginalContainsRangeHeader ? new RangeHeaderValue(
		        rangeValue) : null;
		rangeFetchContents.put(contentRange.getFirstBytePos(), ev.content);
		if (logger.isDebugEnabled())
		{
			logger.debug("Save range content:" + contentRange);
		}
		while (rangeFetchContents.containsKey(waitingWriteStreamPos))
		{
			Buffer content = rangeFetchContents.remove(waitingWriteStreamPos);
			ChannelBuffer buf = ChannelBuffers.wrappedBuffer(
			        content.getRawBuffer(), content.getReadIndex(),
			        content.readableBytes());
			if (logger.isDebugEnabled())
			{
				logger.debug("Write content-range content with stream pos:"
				        + waitingWriteStreamPos);
			}
			getRangeChunkedInput().offer(buf);
			waitingWriteStreamPos = contentRange.getLastBytePos() + 1;
		}
		if (logger.isDebugEnabled())
		{
			logger.debug("After writing conten-range contents, waitingWriteStreamPos = "
			        + waitingWriteStreamPos);
			logger.debug("rangeFetchContents is  " + rangeFetchContents);
		}
		rangeFetch(range, contentRange.getInstanceLength(), 1);
	}

	private HttpResponse buildHttpResponse(HTTPResponseEvent ev, HttpChunk chunk)
	{
		if (null == ev)
		{
			return new DefaultHttpResponse(HttpVersion.HTTP_1_1,
			        HttpResponseStatus.REQUEST_TIMEOUT);
		}

		int status = ev.statusCode;
		if (!isOriginalContainsRangeHeader
		        && HttpResponseStatus.PARTIAL_CONTENT.getCode() == status)
		{
			status = HttpResponseStatus.OK.getCode();
		}
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
		        HttpResponseStatus.valueOf(status));

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
			if (!isOriginalContainsRangeHeader)
			{
				response.removeHeader(HttpHeaders.Names.CONTENT_RANGE);
				response.removeHeader(HttpHeaders.Names.ACCEPT_RANGES);
				response.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
				        String.valueOf(contentRange.getInstanceLength()));
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
				        contentRange.toString());
				response.setHeader(
				        HttpHeaders.Names.CONTENT_LENGTH,
				        ""
				                + (contentRange.getLastBytePos()
				                        - contentRange.getFirstBytePos() + 1));
			}
		}
		else
		{
			response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, ""
			        + ev.content.readableBytes());
		}

		if (ev.content.readable())
		{
			ChannelBuffer bufer = ChannelBuffers.wrappedBuffer(
			        ev.content.getRawBuffer(), ev.content.getReadIndex(),
			        ev.content.readableBytes());
			// response.setChunked(false);
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
		HttpChunk chunk = new DefaultHttpChunk(ChannelBuffers.EMPTY_BUFFER);
		HttpResponse response = buildHttpResponse(ev, chunk);
		localHTTPChannel.write(response);
		if (logger.isDebugEnabled())
		{
			logger.debug("Write response:" + response);
		}
		if (null != contentRange
		        && contentRange.getLastBytePos() < (contentRange
		                .getInstanceLength() - 1))
		{
			String rangeHeaderValue = proxyEvent
			        .getHeader(HttpHeaders.Names.RANGE);
			RangeHeaderValue range = null;
			if (isOriginalContainsRangeHeader)
			{
				range = new RangeHeaderValue(rangeHeaderValue);
				if (range.getLastBytePos() >= contentRange.getLastBytePos())
				{
					status = ProxySessionStatus.SESSION_COMPLETED;
					return;
				}
			}
			waitingFetchStreamPos = contentRange.getLastBytePos() + 1;
			status = ProxySessionStatus.WAITING_MULTI_RANGE_RESPONSE;
			rangeFetch(range, contentRange.getInstanceLength(), -1);
			SharedObjectHelper.getGlobalThreadPool().submit(new Runnable()
			{
				@Override
				public void run()
				{
					localHTTPChannel.write(getRangeChunkedInput());
				}
			});

		}
		else
		{
			status = ProxySessionStatus.SESSION_COMPLETED;
		}
	}

	private void handleRangeUploadResponse(HTTPResponseEvent ev)
	{
		ContentRangeHeaderValue contentRange = null;
		String contentRangeStr = ev.getHeader(HttpHeaders.Names.CONTENT_RANGE);
		if (null != contentRangeStr)
		{
			contentRange = new ContentRangeHeaderValue(contentRangeStr);
			if (contentRange.getLastBytePos() == contentRange
			        .getInstanceLength() - 1)
			{
				HttpChunk chunk = new DefaultHttpChunk(
				        ChannelBuffers.EMPTY_BUFFER);
				HttpResponse response = buildHttpResponse(ev, chunk);
				response.removeHeader(HttpHeaders.Names.CONTENT_RANGE);
				response.removeHeader(HttpHeaders.Names.ACCEPT_RANGES);
				// response.setStatus(HttpResponseStatus.OK);
				localHTTPChannel.write(response);
				// if (response.isChunked())
				// {
				// localHTTPChannel.write(chunk);
				// }
			}
			else
			{
				int fetchSizeLimit = GAEClientConfiguration.getInstance()
				        .getFetchLimitSize();
				long start = contentRange.getLastBytePos() + 1;
				long end = start + fetchSizeLimit - 1;
				if (end >= contentRange.getInstanceLength() - 1)
				{
					end = contentRange.getInstanceLength() - 1;
				}
				contentRange.setLastBytePos(end);
				proxyEvent.setHeader(HttpHeaders.Names.CONTENT_RANGE,
				        contentRange.toString());
				proxyEvent.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
				        String.valueOf(end - start + 1));
				completeProxyRequest(uploadBuffer);
			}
		}
	}

	public void handleResponse(Event res)
	{
		if (res instanceof HTTPResponseEvent)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Handle received HTTP response event.");
			}
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
				case WATING_RANGE_UPLOAD_RESPONSE:
				{
					handleRangeUploadResponse((HTTPResponseEvent) res);
					break;
				}
				default:
				{
					logger.error("Can not handle response event at state:"
					        + status);
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
				close(null);
			}
		}
		if (status.equals(ProxySessionStatus.SESSION_COMPLETED))
		{
			close(null);
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
		isOriginalContainsRangeHeader = proxyEvent
		        .containsHeader(HttpHeaders.Names.RANGE);
		int fetchSizeLimit = GAEClientConfiguration.getInstance()
		        .getFetchLimitSize();
		if (proxyEvent.getContentLength() > GAEConstants.APPENGINE_HTTP_BODY_LIMIT)
		{
			ContentRangeHeaderValue contentRange = new ContentRangeHeaderValue(
			        0, fetchSizeLimit - 1, event.getContentLength());
			proxyEvent.setHeader(HttpHeaders.Names.CONTENT_RANGE,
			        String.valueOf(contentRange));
			proxyEvent.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
			        String.valueOf(fetchSizeLimit - 1));
		}
		else
		{
			if (GAEClientConfiguration.getInstance()
			        .isInjectRangeHeaderSitesMatchHost(
			                proxyEvent.getHeader(HttpHeaders.Names.HOST)))
			{
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
				status = ProxySessionStatus.WAITING_NORMAL_RESPONSE;
				getClientConnection(event).send(event);
			}
		}
	}

	private void completeProxyRequest(Buffer buffer)
	{
		int length = proxyEvent.getContentLength();
		int rest = length - proxyEvent.content.readableBytes();
		proxyEvent.content.write(buffer, rest);
		if (isProxyRequestReady(proxyEvent))
		{
			getClientConnection(proxyEvent).send(proxyEvent);
			status = ProxySessionStatus.WAITING_NORMAL_RESPONSE;
		}
		if (buffer.readable() && buffer != uploadBuffer)
		{
			uploadBuffer.write(buffer, buffer.readableBytes());
		}
		if (proxyEvent.containsHeader(HttpHeaders.Names.CONTENT_RANGE))
		{
			status = ProxySessionStatus.WATING_RANGE_UPLOAD_RESPONSE;
		}
	}

	private void completeProxyRequest(HTTPChunkEvent event)
	{
		completeProxyRequest(Buffer.wrapReadableContent(event.content));
	}

	public void handle(HTTPChunkEvent event)
	{
		if (!isProxyRequestReady(proxyEvent))
		{
			completeProxyRequest(event);
		}
		else
		{
			uploadBuffer.write(event.content);
		}
	}

	public void close(HttpResponse res)
	{
		waitingWriteStreamPos = -1;
		// rangeUploadingEnable = false;
		rangeFetchContents.clear();
		cancelAllRangeRetryTask();
		// ProxySessionManager.getInstance().removeSession(this);
		GAEEventHelper.releaseSessionBuffer(sessionID);
		switch (status)
		{
			case WAITING_NORMAL_RESPONSE:
			{
				if (null != localHTTPChannel)
				{
					if (null == res)
					{
						res = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
						        HttpResponseStatus.REQUEST_TIMEOUT);
						logger.error("Send fake 408 to browser since session closed while no response sent.");
					}
					localHTTPChannel.write(res);

				}
				break;
			}
			case WAITING_MULTI_RANGE_RESPONSE:
			{
				if (null != localHTTPChannel)
				{
					localHTTPChannel.close();
				}
				break;
			}
			default:
				break;
		}
	}
}
