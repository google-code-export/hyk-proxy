/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: XmppAccount.java 
 *
 * @author yinqiwen [ 2010-1-31 | 10:50:02 AM]
 *
 */
package org.hyk.proxy.core.httpserver;

import java.util.Map;

import org.arch.common.KeyValuePair;
import org.arch.event.EventDispatcher;
import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yinqiwen
 * 
 */
public class HttpLocalProxyRequestHandler extends SimpleChannelUpstreamHandler
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private boolean isReadingChunks = false;
	private boolean ishttps = false;

	private Channel localChannel = null;

	public HttpLocalProxyRequestHandler()
	{

	}

	private HTTPRequestEvent buildEvent(HttpRequest request)
	{
		HTTPRequestEvent event = new HTTPRequestEvent();
		event.method = request.getMethod().getName();
		event.url = request.getUri();
		ChannelBuffer content = request.getContent();
		if (null != content)
		{
			event.content = new byte[content.readableBytes()];
			content.readBytes(event.content);
		}
		for (Map.Entry<String, String> header : request.getHeaders())
		{
			event.headers.add(new KeyValuePair<String, String>(header.getKey(),
			        header.getValue()));
		}
		return event;
	}

	private void handleChunks(MessageEvent e)
	{
		HTTPChunkEvent event = new HTTPChunkEvent();
		event.isHttpsChunk = ishttps;
		ChannelBuffer content = null;
		if (e.getMessage() instanceof HttpChunk)
		{
			HttpChunk chunk = (HttpChunk) e.getMessage();
			content = chunk.getContent();
		}
		else if (e.getMessage() instanceof ChannelBuffer)
		{
			content = (ChannelBuffer) e.getMessage();
		}
		if (null != content)
		{
			event.content = new byte[content.readableBytes()];
			content.readBytes(event.content);
		}
		event.setAttachment(e.getChannel());
		try
		{
			EventDispatcher.getSingletonInstance().dispatch(event);
		}
		catch (Exception ex)
		{
			// TODO Auto-generated catch block
			logger.error("Failed to dispatch proxy request event.", ex);
			close();
		}
	}

	private void handleHttpRequest(HttpRequest request, MessageEvent e)
	{
		if (request.getMethod().equals(HttpMethod.CONNECT))
		{
			ishttps = true;
		}
		if (request.isChunked())
		{
			isReadingChunks = true;
		}
		try
		{
			HTTPRequestEvent event = buildEvent(request);
			event.setAttachment(e.getChannel());
			EventDispatcher.getSingletonInstance().dispatch(event);
		}
		catch (Exception ex)
		{
			logger.error("Failed to dispatch proxy request event.", ex);
			close();
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e)
	        throws Exception
	{
		localChannel = e.getChannel();

		if (e.getMessage() instanceof HttpRequest)
		{
			HttpRequest request = (HttpRequest) e.getMessage();
			handleHttpRequest(request, e);
		}
		else
		{
			handleChunks(e);
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
	        throws Exception
	{
		super.channelClosed(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
	        throws Exception
	{
		logger.error("exceptionCaught.", e.getCause());
	}

	public void close()
	{
		if (localChannel != null && localChannel.isConnected())
		{
			localChannel.close();
			localChannel = null;
		}
	}

}
