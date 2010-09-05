/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: XmppAccount.java 
 *
 * @author yinqiwen [ 2010-1-31 | 10:50:02 AM]
 *
 */
package com.hyk.proxy.framework.httpserver;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.event.HttpProxyEvent;
import com.hyk.proxy.framework.event.HttpProxyEventCallback;
import com.hyk.proxy.framework.event.HttpProxyEventService;
import com.hyk.proxy.framework.event.HttpProxyEventType;

/**
 * @author yinqiwen
 * 
 */
@ChannelPipelineCoverage("one")
public class HttpLocalProxyRequestHandler extends SimpleChannelUpstreamHandler implements HttpProxyEventCallback
{
	protected Logger				logger			= LoggerFactory.getLogger(getClass());

	private boolean					isReadingChunks	= false;
	private boolean					ishttps			= false;

	private HttpProxyEventService	eventService;
	private Channel localChannel = null;

	public HttpLocalProxyRequestHandler(HttpProxyEventService eventService)
	{
		this.eventService = eventService;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception
	{
		localChannel = e.getChannel();
		if(null == eventService)
		{
			e.getChannel().write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_IMPLEMENTED)).addListener(ChannelFutureListener.CLOSE);
			return;
		}
		if(!isReadingChunks)
		{
			if(!ishttps)
			{
				HttpRequest request = (HttpRequest)e.getMessage();
				if(request.getMethod().equals(HttpMethod.CONNECT))
				{
					ishttps = true;
				}
				if(request.isChunked())
				{
					isReadingChunks = true;
				}
				HttpProxyEventType type = ishttps?HttpProxyEventType.RECV_HTTPS_REQUEST:HttpProxyEventType.RECV_HTTP_REQUEST;
				HttpProxyEvent event = new HttpProxyEvent(type, request, e.getChannel());
				eventService.handleEvent(event, this);
			}
			else
			{
				if(e.getMessage() instanceof HttpRequest)
				{
					HttpProxyEvent event = new HttpProxyEvent(HttpProxyEventType.RECV_HTTPS_REQUEST, e.getMessage(), e.getChannel());
					eventService.handleEvent(event, this);
				}
				else
				{
					HttpProxyEvent event = new HttpProxyEvent(HttpProxyEventType.RECV_HTTPS_CHUNK, e.getMessage(), e.getChannel());
					eventService.handleEvent(event, this);
				}	
			}
		}
		else
		{
			HttpChunk chunk = (HttpChunk)e.getMessage();
			if(chunk.isLast())
			{
				isReadingChunks = false;
			}
			HttpProxyEvent event = new HttpProxyEvent(HttpProxyEventType.RECV_HTTP_CHUNK, chunk, e.getChannel());
			eventService.handleEvent(event, this);
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		if(null != eventService)
		{
			eventService.close();
			eventService = null;
		}
		super.channelClosed(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
	{
		logger.error("exceptionCaught.", e.getCause());
		if(null != eventService)
		{
			eventService.close();
			eventService = null;
		}
		if(e.getChannel().isOpen())
		{
			e.getChannel().close();
		}
	}

	private void close()
	{
		if(localChannel != null && localChannel.isConnected())
		{
			localChannel.close();
			localChannel = null;
		}
	}
	
	@Override
    public void onEventServiceClose(HttpProxyEventService service)
    {
		close();
    }

	@Override
    public void onProxyEventFailed(HttpProxyEventService service,
            HttpResponse response, HttpProxyEvent event)
    {
		close();
    }

}
