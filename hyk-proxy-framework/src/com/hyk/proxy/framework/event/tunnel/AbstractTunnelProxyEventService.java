/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventService.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:14:39 AM ]
 *
 */
package com.hyk.proxy.framework.event.tunnel;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.event.HttpProxyEvent;
import com.hyk.proxy.framework.event.HttpProxyEventService;
import com.hyk.proxy.framework.event.HttpProxyEventCallback;
import com.hyk.proxy.framework.event.HttpProxyEventType;
import com.hyk.proxy.framework.security.SimpleEncrypter;

/**
 *
 */
public abstract class AbstractTunnelProxyEventService implements
        HttpProxyEventService
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Channel localChannel;

	protected Channel remoteChannel;

	protected boolean isHttps;

	public AbstractTunnelProxyEventService()
	{
	}

	public interface CallBack
	{
		public void callback(Channel remote) throws Exception;
	}

	@Override
	public void close() throws Exception
	{
		closeChannel(localChannel);
		closeChannel(remoteChannel);
	}

	protected void closeChannel(Channel channel)
	{
		if (null != channel && channel.isConnected())
		{
			channel.close();
		}
	}

	protected abstract void getRemoteChannel(HttpProxyEvent event,
	        CallBack callack) throws Exception;

	protected ChannelDownstreamHandler getEncrypter()
	{
		return new SimpleEncrypter.SimpleEncryptEncoder();
	}

	protected ChannelUpstreamHandler getDecrypter()
	{
		return new SimpleEncrypter.SimpleDecryptDecoder();
	}

	protected void initCodecHandler(Channel channel)
	{
		ChannelPipeline pipeline = channel.getPipeline();

		if(null != getDecrypter())
		{
			pipeline.addLast("decrypter", getDecrypter());
		}
		
		pipeline.addLast("httpResponseDecoder", new HttpResponseDecoder());
		// pipeline.addLast("aggregator", new HttpChunkAggregator(10240000));
		if(null != getEncrypter())
		{
			pipeline.addLast("encrypter", getEncrypter());
		}
		
		pipeline.addLast("httpRequestEncoder", new HttpRequestEncoder());

		pipeline.addLast("handler", new ForwardResponseHandler());
	}

	protected void removeCodecHandler(ChannelFuture future)
	{
		future.addListener(new ChannelFutureListener()
		{

			@Override
			public void operationComplete(ChannelFuture future) throws Exception
			{
				localChannel.getPipeline().remove("decoder");
				remoteChannel.getPipeline().remove("httpResponseDecoder");
				remoteChannel.getPipeline().remove("httpRequestEncoder");
				localChannel.getPipeline().remove("encoder");
			}

		});
	}

	protected boolean needForwardConnect()
	{
		return true;
	}

	protected boolean forceShortConnection()
	{
		return true;
	}
	
	protected MessageEvent preProcessForwardMessageEvent(MessageEvent event)
	{
		return event;
	}
	
	protected HttpProxyEvent preProcessForwardHttpProxyEvent(HttpProxyEvent event)
	{
		return event;
	}

	@Override
	public void handleEvent(final HttpProxyEvent evt, HttpProxyEventCallback callback)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("Handle event:" + evt.getType());
		}
		final HttpProxyEvent event = preProcessForwardHttpProxyEvent(evt);
		switch (event.getType())
		{
			case RECV_HTTP_REQUEST:
			case RECV_HTTPS_REQUEST:
			{
				localChannel = event.getChannel();

				final HttpRequest request = (HttpRequest) event.getSource();
				if (event.getType().equals(
				        HttpProxyEventType.RECV_HTTPS_REQUEST))
				{
					isHttps = true;
				}
				else
				{
					if (forceShortConnection())
					{
						request.setHeader(HttpHeaders.Names.CONNECTION, "close");
					}
				}
				try
				{

					// remoteChannel = getRemoteChannel(event);
					getRemoteChannel(event, new CallBack()
					{
						@Override
						public void callback(Channel remote) throws Exception
						{
							remoteChannel = remote;
							if (null == remoteChannel)
							{
								close();
								return;
							}
							initCodecHandler(remoteChannel);
							if (!isHttps || needForwardConnect())
							{
								if (logger.isDebugEnabled())
								{
									logger.debug("Send proxy request");
									logger.debug(request.toString());
								}
								remoteChannel.write(request);
							}
							else if (isHttps)
							{
								HttpResponse res = new DefaultHttpResponse(
								        HttpVersion.HTTP_1_1,
								        HttpResponseStatus.OK);
								ChannelFuture future = event.getChannel()
								        .write(res);
								removeCodecHandler(future);
							}

						}
					});

				}
				catch (Exception e)
				{
					logger.error("Failed to create remote channel!", e);
					closeChannel(localChannel);
				}
				break;
			}
			case RECV_HTTP_CHUNK:
			case RECV_HTTPS_CHUNK:
			{
				remoteChannel.write(event.getSource());
				break;
			}
		}
	}

	@ChannelPipelineCoverage("one")
	class ForwardResponseHandler extends SimpleChannelUpstreamHandler
	{
		ChannelFuture locaChannelFuture;

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
		        throws Exception
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Tunnel server close this connection.");
			}
			close();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
		        throws Exception
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Tunnel connection have an exception!.",
				        e.getCause());
			}
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		        throws Exception
		{
			e = preProcessForwardMessageEvent(e);
			if (e.getMessage() instanceof HttpResponse)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Recv proxy response");
					logger.debug(e.getMessage().toString());
				}
//				if (isHttps)
//				{
//					// http request decoder
//					localChannel.getPipeline().remove("decoder");
//				}
				HttpResponse res = (HttpResponse) e.getMessage();
				
				ChannelFuture future = localChannel.write(res);
				if (!res.isChunked() && !isHttps)
				{
					future.addListener(ChannelFutureListener.CLOSE);
				}
				if (isHttps)
				{
					removeCodecHandler(future);
				}

			}
			else if (e.getMessage() instanceof HttpChunk)
			{
				HttpChunk chunk = (HttpChunk) e.getMessage();
				ChannelFuture future = localChannel.write(chunk);
				if (chunk.isLast())
				{
					future.addListener(ChannelFutureListener.CLOSE);
				}
			}
			else
			{
				localChannel.write(e.getMessage());
			}
		}
	}
	
	@ChannelPipelineCoverage("one")
    public class EmptyHandler extends SimpleChannelUpstreamHandler
	{

	}
}
