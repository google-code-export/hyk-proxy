/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventService.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:14:39 AM ]
 *
 */
package spac.event.forward;

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
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.framework.event.HttpProxyEvent;
import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceStateListener;
import com.hyk.proxy.client.framework.event.HttpProxyEventType;

/**
 *
 */
public class ForwardProxyEventService implements HttpProxyEventService
{
	protected Logger						logger	= LoggerFactory.getLogger(getClass());
	protected ClientSocketChannelFactory	factory;

	protected Channel						localChannel;

	protected Channel						remoteChannel;

	protected boolean						isHttps;

	private HttpProxyEvent					originalProxyEvent;

	private InetSocketAddress				remoteAddr;
	private String remoteHost;
	private int remotePort;

	public void setRemoteAddr(String host, int port)
	{
		this.remoteHost = host;
		this.remotePort = port;
		this.remoteAddr = new InetSocketAddress(host, port);
	}

	protected HttpProxyEventServiceStateListener	listener;

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
		if(null != channel && channel.isOpen())
		{
			channel.close();
		}
	}

	protected InetSocketAddress getRemoteAddress(HttpRequest request)
	{
		return remoteAddr;
	}

	protected Channel getRemoteChannel(HttpRequest request) throws InterruptedException
	{
		if(null != remoteChannel && remoteChannel.isOpen())
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
				localChannel = event.getChannel();

				HttpRequest request = (HttpRequest)event.getSource();
				if(event.getType().equals(HttpProxyEventType.RECV_HTTPS_REQUEST))
				{
					isHttps = true;
				}
				try
				{
					if(logger.isDebugEnabled())
					{
						logger.debug("Send proxy request");
						logger.debug(request.toString());
					}
					getRemoteChannel(request);
					remoteChannel.write(request);
				}
				catch(InterruptedException e)
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
		ChannelFuture	locaChannelFuture;

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Third proxy client close this connection.");
			}
			if(null != listener)
			{
				listener.onEventServiceClose(ForwardProxyEventService.this);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Third proxy client connection have an exception!.", e);
			}
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
		{
			if(e.getMessage() instanceof HttpResponse)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Recv proxy response");
					logger.debug(e.getMessage().toString());
				}
				HttpResponse res = (HttpResponse)e.getMessage();
				if(res.getStatus().getCode() >= 400 && listener != null)
				{
					listener.onProxyEventFailed(ForwardProxyEventService.this, originalProxyEvent);
					return;
				}
				if(isHttps)
				{
					// http request decoder
					localChannel.getPipeline().remove("decoder");
				}
				ChannelFuture future = localChannel.write(res);
				if(!res.isChunked() && !isHttps)
				{
					future.addListener(ChannelFutureListener.CLOSE);
				}
				if(isHttps)
				{
					future.addListener(new ChannelFutureListener()
					{

						@Override
						public void operationComplete(ChannelFuture arg0) throws Exception
						{
							remoteChannel.getPipeline().remove("httpResponseDecoder");
							remoteChannel.getPipeline().remove("httpRequestEncoder");
							// http response encoder
							localChannel.getPipeline().remove("encoder");
						}

					});
				}
			}
			else
			{
				localChannel.write(e.getMessage());
			}
		}
	}

	@Override
	public void addHttpProxyEventServiceStateListener(HttpProxyEventServiceStateListener listener)
	{
		this.listener = listener;
	}

	@Override
	public String getIdentifier()
	{
		return remoteHost + ":" + remotePort;
	}
}
