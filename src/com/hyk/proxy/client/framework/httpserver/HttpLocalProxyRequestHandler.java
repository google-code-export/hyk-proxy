/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: XmppAccount.java 
 *
 * @author yinqiwen [ 2010-1-31 | 10:50:02 AM]
 *
 */
package com.hyk.proxy.client.framework.httpserver;

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
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.framework.event.HttpProxyEvent;
import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.client.framework.event.HttpProxyEventType;

/**
 * @author yinqiwen
 * 
 */
@ChannelPipelineCoverage("one")
public class HttpLocalProxyRequestHandler extends SimpleChannelUpstreamHandler
{
	protected Logger				logger			= LoggerFactory.getLogger(getClass());

	private boolean					isReadingChunks	= false;
	private boolean					ishttps			= false;

	private HttpProxyEventService	eventService;

	public HttpLocalProxyRequestHandler(HttpProxyEventService eventService)
	{
		this.eventService = eventService;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception
	{
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
				eventService.handleEvent(event);
			}
			else
			{
				HttpProxyEvent event = new HttpProxyEvent(HttpProxyEventType.RECV_HTTPS_CHUNK, e.getMessage(), e.getChannel());
				eventService.handleEvent(event);
			}
		}
		else
		{
			HttpChunk chunk = (HttpChunk)e.getMessage();
			if(chunk.isLast())
			{
				isReadingChunks = false;
			}
			else
			{
				HttpProxyEvent event = new HttpProxyEvent(HttpProxyEventType.RECV_HTTP_CHUNK, chunk, e.getChannel());
				eventService.handleEvent(event);
			}
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{

		super.channelClosed(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
	{
		logger.error("exceptionCaught.", e.getCause());

	}

}
