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

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.hyk.proxy.framework.event.HttpProxyEvent;
import com.hyk.proxy.framework.event.HttpProxyEventCallback;


/**
 *
 */
class DirectProxyEventService extends ForwardProxyEventService
{
	DirectProxyEventService(ClientSocketChannelFactory factory)
	{
		super(factory);
	}

	
	@Override
	protected InetSocketAddress getRemoteAddress(HttpRequest request)
	{
		return getHostHeaderAddress(request);
	}
	
	@Override
	public void handleEvent(HttpProxyEvent event,  HttpProxyEventCallback callback)
	{
		switch(event.getType())
		{
			case RECV_HTTPS_REQUEST:
			{
				originalProxyEvent = event;
				HttpRequest req = (HttpRequest)event.getSource();
				if(req.getMethod().equals(HttpMethod.CONNECT))
				{
					localChannel = event.getChannel();
					try
					{
						getRemoteChannel(req);
					}
					catch(InterruptedException e)
					{
						logger.error("", e);
					}
					HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
					//event.getChannel().getPipeline().remove("decoder");
					ChannelFuture future = event.getChannel().write(res);
					removeDecoderAndEncoder(future);
					break;
				}
			}
			default:
			{
				super.handleEvent(event, callback);
				break;
			}
		}
	}

}
