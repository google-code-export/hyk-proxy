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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.application.gae.event.GoogleAppEngineHttpProxyEventServiceFactory;
import com.hyk.proxy.client.application.mix.MixApplicationConfig;
import com.hyk.proxy.client.framework.event.HttpProxyEvent;
import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceStateListener;

/**
 *
 */
class MixProxyEventService  implements HttpProxyEventService, HttpProxyEventServiceStateListener
{
	protected Logger					logger			= LoggerFactory.getLogger(getClass());

	private GoogleAppEngineHttpProxyEventServiceFactory mainEventFactory;
	private HttpProxyEventServiceFactory delegatefactory;
	
	private HttpProxyEventService mainEventService;
	private HttpProxyEventService delegateEventService;
	
	public MixProxyEventService(HttpProxyEventServiceFactory delegatefactory, GoogleAppEngineHttpProxyEventServiceFactory mainEventFactory)
	{
		this.delegatefactory = delegatefactory;
		this.mainEventFactory = mainEventFactory;
	}
	

	@Override
	public void handleEvent(HttpProxyEvent event)
	{
		HttpProxyEventService serv = null;
		if(null != mainEventService && mainEventService.isAbleToHandle(event))
		{
			serv = mainEventService;
		}
		else
		{
			if(null != delegateEventService)
			{
				serv = delegateEventService;
			}
			else if(MixApplicationConfig.matchDelegateRules(event))
			{
				delegateEventService = delegatefactory.createHttpProxyEventService();
				delegateEventService.addHttpProxyEventServiceStateListener(this);
				serv = delegateEventService;
			}
			else
			{
				if(null == mainEventService)
				{
					mainEventService = mainEventFactory.createHttpProxyEventService();
					mainEventService.addHttpProxyEventServiceStateListener(this);
				}
				serv = mainEventService;
			}
		}
		serv.handleEvent(event);
	}


	@Override
	public void close() throws Exception
	{
		if(null != mainEventService)
		{
			mainEventService.close();
			mainEventService = null;
		}
		if(null != delegateEventService)
		{
			delegateEventService.close();
			delegateEventService = null;
		}
	}


	@Override
	public boolean isAbleToHandle(HttpProxyEvent event)
	{
		return true;
	}


	@Override
	public void addHttpProxyEventServiceStateListener(HttpProxyEventServiceStateListener listener)
	{		
	}


	@Override
	public void onEventServiceClose(HttpProxyEventService service)
	{
		if(service == mainEventService)
		{
			service = null;
		}
		else if(service == delegateEventService)
		{
			
			delegateEventService = null;
		}
		
	}

}
