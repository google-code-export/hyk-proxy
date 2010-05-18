/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyEvent.java 
 *
 * @author yinqiwen [ 2010-5-13 | 07:51:57 PM]
 *
 */
package com.hyk.proxy.client.framework.event;

import java.util.EventObject;

import org.jboss.netty.channel.Channel;

/**
 *
 */
public class HttpProxyEvent extends EventObject
{
	/**
	 * 
	 */
	Channel channel;
	public Channel getChannel()
	{
		return channel;
	}

	public HttpProxyEventType getType()
	{
		return type;
	}

	HttpProxyEventType	type;

	public HttpProxyEvent(HttpProxyEventType type, Object source, Channel channel)
	{
		super(source);
		this.type = type;
		this.channel = channel;
	}

}
