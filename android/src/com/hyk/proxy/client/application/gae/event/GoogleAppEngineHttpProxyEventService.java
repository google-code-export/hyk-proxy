/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyService.java 
 *
 * @author yinqiwen [ 2010-5-13 | 07:50:44 PM ]
 *
 */
package com.hyk.proxy.client.application.gae.event;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.hyk.proxy.android.config.Config;
import org.hyk.proxy.framework.event.HttpProxyEvent;
import org.hyk.proxy.framework.event.HttpProxyEventCallback;
import org.hyk.proxy.framework.event.HttpProxyEventService;
import org.hyk.proxy.framework.event.HttpProxyEventType;
import org.hyk.proxy.framework.httpserver.reverse.LocalHttpsForwardHandler;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
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

import com.hyk.proxy.client.application.gae.event.GoogleAppEngineHttpProxyEventServiceFactory.FetchServiceSelector;
import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.http.header.ContentRangeHeaderValue;
import com.hyk.proxy.common.http.header.RangeHeaderValue;
import com.hyk.proxy.common.http.message.HttpRequestExchange;
import com.hyk.proxy.common.http.message.HttpResponseExchange;
import com.hyk.proxy.common.rpc.service.AsyncFetchService;
import com.hyk.proxy.common.rpc.service.FetchService;
import com.hyk.rpc.core.RpcCallback;
import com.hyk.rpc.core.RpcCallbackResult;

/**
 *
 */
class GoogleAppEngineHttpProxyEventService implements HttpProxyEventService,
        RpcCallback<HttpResponseExchange>
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private FetchServiceSelector selector;
	private SSLContext sslContext;
	private boolean ishttps;
	private String httpspath;
	private Channel channel;
	private ChannelBuffer lastReadLeftBuffer;
	private LinkedList<ChannelBuffer> chunkedBodys = new LinkedList<ChannelBuffer>();
	private HttpProxyEvent originalProxyEvent;
	private HttpRequestExchange originalRequest;
	private HttpRequestExchange forwardRequest;

	private HttpVersion proxyHttpVer;

	private RangeHttpProxyChunkedInput chunkedInput;

	private Executor workerExecutor;

	private HttpProxyEventCallback callback;

	GoogleAppEngineHttpProxyEventService(FetchServiceSelector selector,
	        Executor workerExecutor)
	{
		this.selector = selector;
		// this.sslContext = sslContext;
		this.workerExecutor = workerExecutor;
	}

	protected boolean isProxyRequestReady()
	{
		int contentLength = forwardRequest.getContentLength();
		if (contentLength > 0
		        && forwardRequest.getBody().length < contentLength)
		{
			return false;
		}
		return true;
	}

	protected void waitForwardBodyComplete() throws InterruptedException
	{
		int contentLength = forwardRequest.getContentLength();
		if (contentLength > 0 && forwardRequest.getBody().length == 0)
		{
			byte[] body = new byte[contentLength];
			int cur = 0;
			int end = body.length;
			while (cur < end)
			{
				int reading = end - cur;
				ChannelBuffer buffer = null;
				if (null != lastReadLeftBuffer)
				{
					buffer = lastReadLeftBuffer;
					lastReadLeftBuffer = null;
				}
				else
				{
					synchronized (chunkedBodys)
					{
						if (chunkedBodys.isEmpty())
						{
							chunkedBodys.wait();
						}
						buffer = chunkedBodys.removeFirst();
					}
				}
				if (buffer.readableBytes() > reading)
				{
					buffer.readBytes(body, cur, reading);
					lastReadLeftBuffer = buffer;
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

	protected HttpRequestExchange buildForwardRequest(HttpRequest recvReq)
	{
		chunkedBodys.clear();
		HttpRequestExchange gaeRequest = new HttpRequestExchange();
		StringBuffer urlbuffer = new StringBuffer();
		if (ishttps)
		{
			urlbuffer.append("https://").append(httpspath);
		}
		else
		{
			if (!recvReq.getUri().toLowerCase().startsWith("http://"))
			{
				urlbuffer.append("http://").append(
				        recvReq.getHeader(HttpHeaders.Names.HOST));
			}
		}
		urlbuffer.append(recvReq.getUri());
		gaeRequest.setUrl(urlbuffer.toString());
		gaeRequest.setMethod(recvReq.getMethod().getName());
		Set<String> headers = recvReq.getHeaderNames();
		boolean containRangeHeader = false;
		for (String headerName : headers)
		{
			if (headerName.equalsIgnoreCase(HttpHeaders.Names.RANGE))
			{
				containRangeHeader = true;
			}
			List<String> headerValues = recvReq.getHeaders(headerName);
			if (null != headerValues)
			{
				for (String headerValue : headerValues)
				{
					gaeRequest.addHeader(headerName, headerValue);
				}
			}
		}

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
		else
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Not Inject a range header for host:"
				        + recvReq.getHeader(HttpHeaders.Names.HOST));
			}
		}
		return gaeRequest;
	}

	@Override
	public void handleEvent(final HttpProxyEvent event,
	        HttpProxyEventCallback callback)
	{
		this.callback = callback;
		if (logger.isDebugEnabled())
		{
			logger.debug("Handle event:" + event.getType() + " in handler:"
			        + hashCode());
		}
		try
		{
			switch (event.getType())
			{
				case RECV_HTTP_REQUEST:
				case RECV_HTTPS_REQUEST:
				{
					this.channel = event.getChannel();
					HttpRequest request = (HttpRequest) event.getSource();
					proxyHttpVer = request.getProtocolVersion();
					this.originalProxyEvent = event;
					ishttps = event.getType().equals(
					        HttpProxyEventType.RECV_HTTPS_REQUEST);
					if (request.getMethod().equals(HttpMethod.CONNECT))
					{
						if (logger.isDebugEnabled())
						{
							logger.debug("Recv https Connect request:"
							        + request);
						}
						httpspath = request.getHeader("Host");
						if (httpspath == null)
						{
							httpspath = request.getUri();
						}
						String httpshost = httpspath;
						String httpsport = "443";
						if (httpspath.indexOf(":") != -1)
						{
							httpshost = httpspath.substring(0,
							        httpspath.indexOf(":"));
							httpsport = httpspath.substring(httpspath
							        .indexOf(":") + 1);
						}
						sslContext = ClientUtils.getFakeSSLContext(httpshost,
						        httpsport);

						// sslContext.getSocketFactory().
						HttpResponse response = new DefaultHttpResponse(
						        proxyHttpVer, HttpResponseStatus.OK);
						event.getChannel().write(response)
						        .addListener(new ChannelFutureListener()
						        {
							        @Override
							        public void operationComplete(
							                ChannelFuture future)
							                throws Exception
							        {
							        	event.getChannel().getPipeline().remove("decoder");
							        	event.getChannel().getPipeline().remove("encoder");
							        	event.getChannel().getPipeline().remove("handler");
							        	event.getChannel().getPipeline().remove("chunkedWriter");
							        	event.getChannel().getPipeline().addLast("forward", new LocalHttpsForwardHandler());
								        // https connection
								        // SSLSocketFactory f =
										// sslContext.getSocketFactory();
								        // SSLSocket s =
										// ClientUtils.getSSLSocket(event.getChannel(),
										// f);
								        // byte[] buff = new byte[4096];
								        // int len =
										// s.getInputStream().read(buff);
								        // System.out.println("###########" +
										// new String(buff, 0, len));
//								        if (event.getChannel().getPipeline()
//								                .get("ssl") == null)
//								        {
//									        InetSocketAddress remote = (InetSocketAddress) event
//									                .getChannel()
//									                .getRemoteAddress();
//									        // SSLEngine engine = sslContext
//									        // .createSSLEngine();
//									        SSLEngine engine = sslContext
//									                .createSSLEngine(remote
//									                        .getAddress()
//									                        .getHostAddress(),
//									                        remote.getPort());
//
//									        engine.setUseClientMode(false);
//									        //ClientUtils.printChoosedAlias(engine);
//									        event.getChannel()
//									                .getPipeline()
//									                .addBefore(
//									                        "decoder",
//									                        "ssl",
//									                        new SslHandler(
//									                                engine));
//								        }
							        }
						        });
					}
					else
					{
						if (null == selector)
						{
							HttpResponse res = new DefaultHttpResponse(
							        HttpVersion.HTTP_1_0,
							        HttpResponseStatus.SERVICE_UNAVAILABLE);
							event.getChannel().write(res);
							return;
						}
						forwardRequest = buildForwardRequest(request);

						asyncFetch(forwardRequest);
					}
					break;
				}
				case RECV_HTTP_CHUNK:
				case RECV_HTTPS_CHUNK:
				{
					HttpChunk chunk = (HttpChunk) event.getSource();
					synchronized (chunkedBodys)
					{
						chunkedBodys.add(chunk.getContent());
						chunkedBodys.notify();
					}
					break;
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to handle this event.", e);
		}

	}

	private void executeAsyncFetch(final HttpRequestExchange req)
	{
		AsyncFetchService fetchService = selector.selectAsync(req);
		fetchService.fetch(req, GoogleAppEngineHttpProxyEventService.this);
		if (logger.isDebugEnabled())
		{
			logger.debug("Send proxy request");
			logger.debug(ClientUtils.httpMessage2String(req));
		}
	}

	protected void asyncFetch(final HttpRequestExchange req)
	{
		if (!isProxyRequestReady())
		{
			workerExecutor.execute(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						waitForwardBodyComplete();
						executeAsyncFetch(req);
					}
					catch (InterruptedException e)
					{
						logger.error("", e);
					}
				}
			});
		}
		else
		{
			executeAsyncFetch(req);
		}
	}

	protected HttpResponseExchange syncFetch(HttpRequestExchange req)
	        throws InterruptedException
	{
		waitForwardBodyComplete();
		FetchService fetchService = selector.select(req);
		if (logger.isDebugEnabled())
		{
			logger.debug("Send proxy request");
			logger.debug(ClientUtils.httpMessage2String(req));
		}
		HttpResponseExchange res = fetchService.fetch(req);
		if (logger.isDebugEnabled())
		{
			logger.debug("Recv proxy response");
			logger.debug(ClientUtils.httpMessage2String(res));
		}
		return res;
	}

	@Override
	public void callBack(RpcCallbackResult<HttpResponseExchange> result)
	{
		try
		{
			HttpResponseExchange forwardResponse = result.get();
			if (logger.isDebugEnabled())
			{
				logger.debug("Recv proxy response");
				logger.debug(ClientUtils.httpMessage2String(forwardResponse));
			}
			int fetchSizeLimit = Config.getInstance().getFetchLimitSize();
			if (forwardResponse.isResponseTooLarge()
			        || forwardResponse.getResponseCode() == 400)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Try to start range fetch!");
				}
				if (!forwardRequest.containsHeader(HttpHeaders.Names.RANGE))
				{
					forwardRequest.setHeader(HttpHeaders.Names.RANGE,
					        new RangeHeaderValue(0, fetchSizeLimit - 1));
				}
				else
				{
					String hv = forwardRequest
					        .getHeaderValue(HttpHeaders.Names.RANGE);
					RangeHeaderValue containedRange = new RangeHeaderValue(hv);
					forwardRequest.setHeader(
					        HttpHeaders.Names.RANGE,
					        new RangeHeaderValue(containedRange
					                .getFirstBytePos(), containedRange
					                .getFirstBytePos() + fetchSizeLimit - 1));
				}
				forwardResponse = syncFetch(forwardRequest);
			}

			// Proxy request with Content-Range Header
			if (forwardRequest.containsHeader(HttpHeaders.Names.CONTENT_RANGE))
			{
				ContentRangeHeaderValue lastContentRange = new ContentRangeHeaderValue(
				        forwardRequest
				                .getHeaderValue(HttpHeaders.Names.CONTENT_RANGE));
				forwardResponse = new RangeHttpProxyChunkedOutput(
				        lastContentRange).execute();
			}

			if (channel.isConnected())
			{
				String contentRangeValue = forwardResponse
				        .getHeaderValue(HttpHeaders.Names.CONTENT_RANGE);
				ContentRangeHeaderValue contentRange = null;
				if (null != contentRangeValue)
				{
					contentRange = new ContentRangeHeaderValue(
					        contentRangeValue);
					forwardResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
					        String.valueOf(contentRange.getInstanceLength()));

					forwardResponse.setResponseCode(200);

					if (!originalRequest
					        .containsHeader(HttpHeaders.Names.RANGE))
					{
						forwardResponse
						        .removeHeader(HttpHeaders.Names.CONTENT_RANGE);
						forwardResponse
						        .removeHeader(HttpHeaders.Names.ACCEPT_RANGES);
					}
					else
					{
						String originalRangeValue = originalRequest
						        .getHeaderValue(HttpHeaders.Names.RANGE);
						RangeHeaderValue originalRange = new RangeHeaderValue(
						        originalRangeValue);
						forwardResponse
						        .removeHeader(HttpHeaders.Names.CONTENT_RANGE);
						ContentRangeHeaderValue returnContentRange = new ContentRangeHeaderValue(
						        contentRange.toString());
						if (originalRange.getLastBytePos() > 0)
						{
							returnContentRange.setLastBytePos(originalRange
							        .getLastBytePos());
						}
						else
						{
							returnContentRange.setLastBytePos(contentRange
							        .getInstanceLength() - 1);
						}
						forwardResponse.setHeader(
						        HttpHeaders.Names.CONTENT_RANGE,
						        returnContentRange);
						forwardResponse
						        .setHeader(
						                HttpHeaders.Names.CONTENT_LENGTH,
						                (returnContentRange.getLastBytePos()
						                        - returnContentRange
						                                .getFirstBytePos() + 1)
						                        + "");
						if (logger.isDebugEnabled())
						{
							logger.debug("Range get response content-range header is "
							        + returnContentRange);
						}
					}

				}
				if (forwardResponse.getResponseCode() == 0)
				{
					forwardResponse.setResponseCode(400);
				}
				HttpResponse response = ClientUtils
				        .buildHttpServletResponse(forwardResponse);
				if (forwardResponse.getResponseCode() >= 400)
				{
					if (callback != null)
					{
						callback.onProxyEventFailed(this, response,
						        originalProxyEvent);
						return;
					}
				}
				ChannelFuture future = channel.write(response);
				// future.await();
				if (null != contentRange
				        && contentRange.getLastBytePos() < (contentRange
				                .getInstanceLength() - 1))
				{
					chunkedInput = new RangeHttpProxyChunkedInput(selector,
					        workerExecutor, forwardRequest,
					        contentRange.getLastBytePos() + 1,
					        contentRange.getInstanceLength());
					future = channel.write(chunkedInput);
				}
				// future.addListener(ChannelFutureListener.CLOSE);
			}
			else
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Warn:Browser connection is already closed by browser.");
				}
			}
		}
		catch (Throwable e)
		{
			logger.error("Encounter error for request:" + forwardRequest.url, e);
			if (channel.isConnected())
			{
				channel.write(
				        new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				                HttpResponseStatus.REQUEST_TIMEOUT))
				        .addListener(ChannelFutureListener.CLOSE);
			}
		}
	}

	class RangeHttpProxyChunkedOutput
	{
		private ContentRangeHeaderValue lastContentRange;

		public RangeHttpProxyChunkedOutput(
		        ContentRangeHeaderValue lastContentRange)
		{
			this.lastContentRange = lastContentRange;
		}

		public HttpResponseExchange execute() throws Exception
		{
			HttpResponseExchange forwardResponse = null;
			int fetchSizeLimit = Config.getInstance().getFetchLimitSize();
			while (null != lastContentRange)
			{
				forwardRequest.setBody(new byte[0]);
				ContentRangeHeaderValue old = lastContentRange;
				long sendSize = fetchSizeLimit;
				if (old.getInstanceLength() - old.getLastBytePos() - 1 < fetchSizeLimit)
				{
					sendSize = (old.getInstanceLength() - old.getLastBytePos() - 1);
				}
				if (sendSize <= 0)
				{
					break;
				}
				lastContentRange = new ContentRangeHeaderValue(
				        old.getLastBytePos() + 1, old.getLastBytePos()
				                + sendSize, old.getInstanceLength());
				forwardRequest.setHeader(HttpHeaders.Names.CONTENT_RANGE,
				        lastContentRange);
				forwardRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
				        String.valueOf(sendSize));
				forwardResponse = syncFetch(forwardRequest);
				if (sendSize < fetchSizeLimit)
				{
					lastContentRange = null;
				}
			}
			return forwardResponse;
		}
	}

	private void closeChannel(Channel channel)
	{
		if (null != channel && channel.isOpen())
		{
			channel.close();
		}
	}

	@Override
	public void close() throws Exception
	{
		originalRequest = null;
		if (chunkedInput != null)
		{
			chunkedInput.close();
		}
		closeChannel(channel);
	}

}
