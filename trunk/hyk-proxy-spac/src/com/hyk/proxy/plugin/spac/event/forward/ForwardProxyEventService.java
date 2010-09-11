/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventService.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:14:39 AM ]
 *
 */
package com.hyk.proxy.plugin.spac.event.forward;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;

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
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.event.HttpProxyEvent;
import com.hyk.proxy.framework.event.HttpProxyEventCallback;
import com.hyk.proxy.framework.event.HttpProxyEventService;
import com.hyk.proxy.framework.event.HttpProxyEventType;

/**
 *
 */
public class ForwardProxyEventService implements HttpProxyEventService
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected ClientSocketChannelFactory factory;

	protected Channel localChannel;

	protected Channel remoteChannel;

	protected boolean isHttps;

	protected HttpProxyEvent originalProxyEvent;

	private InetSocketAddress remoteAddr;

	private HttpProxyEventCallback callback;

	public void setRemoteAddr(String host, int port)
	{
		this.remoteAddr = new InetSocketAddress(host, port);
	}

	public ForwardProxyEventService(ClientSocketChannelFactory factory)
	{
		this.factory = factory;
	}

	@Override
	public void close() throws Exception
	{
		closeChannel(remoteChannel);
	}

	private void closeChannel(Channel channel)
	{
		if (null != channel && channel.isOpen())
		{
			channel.close();
		}
	}

	protected InetSocketAddress getRemoteAddress(HttpRequest request)
	{
		if(null == remoteAddr)
		{
			remoteAddr = getHostHeaderAddress(request);
		}
		return remoteAddr;
	}
	
	protected InetSocketAddress getHostHeaderAddress(HttpRequest request)
	{
		String host = request.getHeader("Host");
		if(null == host)
		{
			String url = request.getUri();
			if(url.startsWith("http://"))
			{
				url = url.substring(7);
				int next = url.indexOf("/");
				host = url.substring(0, next);
			}
			else
			{
				host = url;
			}
		}
		int index = host.indexOf(":");
		int port = 80;
		if(request.getMethod().equals(HttpMethod.CONNECT))
		{
			port = 443;
		}
		String hostValue = host;
		if(index > 0)
		{
			hostValue = host.substring(0, index).trim();
			port = Integer.parseInt(host.substring(index + 1).trim());
		}
        if(logger.isDebugEnabled())
        {
        	logger.debug("Get remote address " + host + ":" + port);
        }
		return new InetSocketAddress(hostValue, port);
	}

	protected Channel getRemoteChannel(HttpRequest request)
	        throws InterruptedException
	{
		if (null != remoteChannel && remoteChannel.isOpen())
		{
			return remoteChannel;
		}
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("httpResponseDecoder", new HttpResponseDecoder());
		pipeline.addLast("httpRequestEncoder", new HttpRequestEncoder());

		pipeline.addLast("handler", new ForwardResponseHandler());
		remoteChannel = factory.newChannel(pipeline);
		remoteChannel.connect(getRemoteAddress(request)).await();
		// remoteChannels.put(key, value)
		return remoteChannel;
	}

	@Override
	public void handleEvent(HttpProxyEvent event,
	        HttpProxyEventCallback callback)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("Handle event:" + event.getType());
		}
		this.callback = callback;
		switch (event.getType())
		{
			case RECV_HTTP_REQUEST:
			case RECV_HTTPS_REQUEST:
			{
				localChannel = event.getChannel();
				originalProxyEvent = event;
				HttpRequest request = (HttpRequest) event.getSource();
				if (event.getType().equals(
				        HttpProxyEventType.RECV_HTTPS_REQUEST))
				{
					isHttps = true;
				}
				try
				{
					if (logger.isDebugEnabled())
					{
						logger.debug("Send proxy request");
						logger.debug(request.toString());
					}
					getRemoteChannel(request);
					remoteChannel.write(request);
				}
				catch (InterruptedException e)
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

	protected void removeDecoderAndEncoder(ChannelFuture future)
	{
		future.addListener(new ChannelFutureListener()
		{

			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception
			{
				localChannel.getPipeline().remove("decoder");
				remoteChannel.getPipeline().remove("httpResponseDecoder");
				remoteChannel.getPipeline().remove("httpRequestEncoder");
				// http response encoder
				localChannel.getPipeline().remove("encoder");
			}

		});
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
				logger.debug("Third proxy client close this connection.");
			}
			if (null != callback)
			{
				callback.onEventServiceClose(ForwardProxyEventService.this);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
		        throws Exception
		{
			if (logger.isDebugEnabled())
			{
				logger.debug(
				        "Third proxy client connection have an exception!.", e.getCause());
			}
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		        throws Exception
		{
			if (e.getMessage() instanceof HttpResponse)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Recv proxy response");
					logger.debug(e.getMessage().toString());
				}
				HttpResponse res = (HttpResponse) e.getMessage();
				if (res.getStatus().getCode() >= 400 && callback != null)
				{
					callback.onProxyEventFailed(ForwardProxyEventService.this,
					        res, originalProxyEvent);
					return;
				}

				ChannelFuture future = localChannel.write(res);
				if (isHttps)
				{
					removeDecoderAndEncoder(future);
				}
			}
			else
			{
				localChannel.write(e.getMessage());
			}
		}
	}

}
