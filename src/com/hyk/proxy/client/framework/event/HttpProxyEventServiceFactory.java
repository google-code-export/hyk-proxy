/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyEventServiceFactory.java 
 *
 * @author yinqiwen [ 2010-5-13 | 08:47:48 PM ]
 *
 */
package com.hyk.proxy.client.framework.event;

/**
 *
 */
public interface HttpProxyEventServiceFactory
{
//	public boolean isableToHandle(HttpProxyEvent event);
	
	public HttpProxyEventService createHttpProxyEventService();
	
	public void close();
}
