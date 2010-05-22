/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventService.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:14:39 AM ]
 *
 */
package com.hyk.proxy.client.application.mix.event;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.application.gae.event.GoogleAppEngineHttpProxyEventServiceFactory;
import com.hyk.proxy.client.application.mix.MixApplicationConfig;
import com.hyk.proxy.client.framework.event.HttpProxyEvent;
import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.client.framework.event.HttpProxyEventType;

/**
 *
 */
class MixProxyEventService  implements HttpProxyEventService
{
	protected Logger					logger			= LoggerFactory.getLogger(getClass());

	private GoogleAppEngineHttpProxyEventServiceFactory mainEventFactory;
	private HttpProxyEventServiceFactory delegatefactory;
	
	private HttpProxyEventService wrapEventService;
	
	public MixProxyEventService(HttpProxyEventServiceFactory delegatefactory, GoogleAppEngineHttpProxyEventServiceFactory mainEventFactory)
	{
		this.delegatefactory = delegatefactory;
		this.mainEventFactory = mainEventFactory;
	}
	

	@Override
	public void handleEvent(HttpProxyEvent event)
	{
		if(null == wrapEventService)
		{
			HttpProxyEventType[] types = MixApplicationConfig.getHttpProxyEventTypes();
			//System.out.println("####" + types.length);
			for(HttpProxyEventType type:types)
			{
				if(type.equals(event.getType()))
				{
					wrapEventService = delegatefactory.createHttpProxyEventService();
					break;
				}
			}
		}
		//if(null == wrapEventService)
		{
			String[] urlPatterns = MixApplicationConfig.getDelegateURLPattern();
			switch(event.getType())
			{
				case RECV_HTTP_REQUEST:
				case RECV_HTTPS_REQUEST:
				{
					HttpRequest req = (HttpRequest)event.getSource();
					for(String pattern:urlPatterns)
					{
						pattern = pattern.trim();
						if(!pattern.isEmpty())
						{
							if(req.getUri().contains(pattern))
							{
								wrapEventService = delegatefactory.createHttpProxyEventService();
								break;
							}
						}
					}
					break;
				}
			}
		}
		//if(null == wrapEventService)
		{
			String[] delegateMethods = MixApplicationConfig.getDelegateMethods();
			switch(event.getType())
			{
				case RECV_HTTP_REQUEST:
				case RECV_HTTPS_REQUEST:
				{
					HttpRequest req = (HttpRequest)event.getSource();
					for(String method:delegateMethods)
					{
						if(req.getMethod().getName().equalsIgnoreCase(method.trim()))
						{
							wrapEventService = delegatefactory.createHttpProxyEventService();
							break;
						}
					}
					break;
				}
			}
		}
		if(null == wrapEventService)
		{
			wrapEventService = mainEventFactory.createHttpProxyEventService();
		}
		wrapEventService.handleEvent(event);
	}


	@Override
	public void close() throws Exception
	{
		if(null != wrapEventService)
		{
			wrapEventService.close();
		}
	}

}
