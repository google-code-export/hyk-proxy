/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventService.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:14:39 AM ]
 *
 */
package org.hyk.proxy.spac.handler.forward;


import java.net.InetSocketAddress;

import org.arch.event.Event;
import org.arch.event.EventHeader;
import org.arch.event.NamedEventHandler;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
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



/**
 *
 */
public class ForwardProxyEventHandler implements NamedEventHandler
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected ClientSocketChannelFactory factory;

	protected Channel localChannel;

	protected Channel remoteChannel;

	protected boolean isHttps;

	private InetSocketAddress remoteAddr;

	public void setRemoteAddr(String host, int port)
	{
		this.remoteAddr = new InetSocketAddress(host, port);
	}

	protected ForwardProxyEventHandler(ClientSocketChannelFactory factory)
	{
		this.factory = factory;
	}

	@Override
    public void onEvent(EventHeader header, Event event)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public String getName()
    {
	    return "FORWARD";
    }

}
