/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyEventServiceStateListener.java 
 *
 * @author yinqiwen [ 2010-5-30 | 02:40:03 PM ]
 *
 */
package com.hyk.proxy.client.framework.event;

/**
 *
 */
public interface HttpProxyEventServiceStateListener
{
	public void onEventServiceClose(HttpProxyEventService service);
	
	public void onProxyEventFailed(HttpProxyEventService service, HttpProxyEvent event);
}
