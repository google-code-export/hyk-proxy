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

import java.net.InetSocketAddress;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;

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
		}

		int index = host.indexOf(":");
		int port = 80;
		String hostValue = host;
		if(index > 0)
		{
			hostValue = host.substring(0, index).trim();
			port = Integer.parseInt(host.substring(index + 1).trim());
		}

		return new InetSocketAddress(hostValue, port);
	}
	
	@Override
	public String getIdentifier()
	{
		return DirectProxyEventServiceFactory.NAME;
	}

}
