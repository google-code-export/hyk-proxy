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

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.hyk.proxy.client.framework.event.HttpProxyEvent;

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
	
	@Override
	public String getIdentifier()
	{
		return DirectProxyEventServiceFactory.NAME;
	}
	
	@Override
	public void handleEvent(HttpProxyEvent event)
	{
		switch(event.getType())
		{
			case RECV_HTTPS_REQUEST:
			{
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
				super.handleEvent(event);
				break;
			}
		}
	}

}
