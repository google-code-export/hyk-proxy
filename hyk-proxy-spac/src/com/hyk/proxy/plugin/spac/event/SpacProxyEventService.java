/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventService.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:14:39 AM ]
 *
 */
package com.hyk.proxy.plugin.spac.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.event.HttpProxyEvent;
import com.hyk.proxy.framework.event.HttpProxyEventCallback;
import com.hyk.proxy.framework.event.HttpProxyEventService;
import com.hyk.proxy.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.framework.event.HttpProxyEventType;
import com.hyk.proxy.plugin.spac.event.forward.DirectProxyEventServiceFactory;
import com.hyk.proxy.plugin.spac.event.forward.ForwardProxyEventService;
import com.hyk.proxy.plugin.spac.event.forward.ForwardProxyEventServiceFactory;
import com.hyk.util.net.NetUtil;


/**
 *
 */
class SpacProxyEventService  implements HttpProxyEventService, HttpProxyEventCallback
{
	protected Logger					logger			= LoggerFactory.getLogger(getClass());

	SpacProxyEventServiceFactory ownFactory;
	
	private HttpProxyEventServiceFactory delegateFactory;
	private HttpProxyEventService delegate;
	
	
	private HttpProxyEventCallback callback;
	
	public SpacProxyEventService(SpacProxyEventServiceFactory ownFactory)
	{
		this.ownFactory = ownFactory;
	}
	
	private void selectProxy(String proxy, HttpProxyEvent event)
	{
		if(null == proxy)
		{
			HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);	
			event.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
			return;
		}
//		if(null != delegate)
//		{
//			historys.put(delegate.getIdentifier(), delegate);
//			delegate = null;
//		}
		delegateFactory = HttpProxyEventServiceFactory.Registry.getHttpProxyEventServiceFactory(proxy);
//		if(historys.containsKey(proxy))
//		{
//			delegate = historys.get(proxy);
//		}
		if(null == delegateFactory)
		{
			if(proxy.indexOf(":") != -1)
			{
				delegateFactory = HttpProxyEventServiceFactory.Registry.getHttpProxyEventServiceFactory(ForwardProxyEventServiceFactory.NAME);
				if(null == delegate)
				{
					int index = proxy.indexOf(":");
					String host = proxy.substring(0, index).trim();
					int port = Integer.parseInt(proxy.substring(index + 1).trim());
					ForwardProxyEventService es = (ForwardProxyEventService)delegateFactory.createHttpProxyEventService();
					es.setRemoteAddr(host, port);
					delegate = es;
				}			
			}
		}
		else
		{
			if(null != delegate)
			{
				try
                {
	                delegate.close();
                }
                catch (Exception e)
                {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
			}
			delegate = delegateFactory.createHttpProxyEventService();
		}
		if(logger.isDebugEnabled())
		{
			logger.debug("Delegate proxy to :" + delegateFactory.getName());
		}
		//delegate.addHttpProxyEventServiceStateListener(this);
		delegate.handleEvent(event, this);
	}
	
	@Override
	public void handleEvent(HttpProxyEvent event, HttpProxyEventCallback callback)
	{
		this.callback = callback;
		switch(event.getType())
		{
			case RECV_HTTP_REQUEST:
			case RECV_HTTPS_REQUEST:
			{
				HttpRequest req = (HttpRequest)event.getSource();
				String protocol = "http";
				if(event.getType().equals(HttpProxyEventType.RECV_HTTPS_REQUEST))
				{
					protocol = "https";
				}
				String proxy = null;
				String host = req.getHeader("Host");
				if(null != host)
				{
					int index = host.indexOf(":");
					if(index > -1)
					{
						host = host.substring(0, index);
					}
					if(NetUtil.isPrivateIP(host))
					{
						proxy = DirectProxyEventServiceFactory.NAME;
					}
				}
				if(null == proxy)
				{
					proxy = (String)ownFactory.csl.invoke("firstSelectProxy", new Object[]{protocol, req.getMethod().toString(), req.getUri(), req});
				}
				
				if(logger.isDebugEnabled())
				{
					logger.debug("Select a proxy:" + proxy);
				}
				selectProxy(proxy, event);
				break;
			}
			default:
			{
				if(null != delegate)
				{
					delegate.handleEvent(event, this);
				}
				break;
			}
		}
		
	}


	@Override
	public void close() throws Exception
	{
		if(null != delegate)
		{
			delegate.close();
		}
	}

	@Override
	public void onEventServiceClose(HttpProxyEventService service)
	{
		if(null != callback)
		{
			callback.onEventServiceClose(service);
		}
		//historys.remove(service.getIdentifier());
	}

	@Override
	public void onProxyEventFailed(HttpProxyEventService service, HttpResponse res, HttpProxyEvent event)
	{
		//String identifier = service.getIdentifier();
		String proxy = (String)ownFactory.csl.invoke("reselectProxyWhenFailed", new Object[]{res, delegateFactory.getName()});
		if(null == proxy)
		{
			event.getChannel().write(res).addListener(ChannelFutureListener.CLOSE);
			return;
		}
		selectProxy(proxy, event);
	}

}
