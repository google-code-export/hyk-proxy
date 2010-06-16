/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyService.java 
 *
 * @author yinqiwen [ 2010-5-13 | 07:50:44 PM ]
 *
 */
package com.hyk.proxy.client.framework.event;

/**
 *
 */
public interface HttpProxyEventService
{
	public String getIdentifier();
	
	public void addHttpProxyEventServiceStateListener(HttpProxyEventServiceStateListener listener);
	
	public void handleEvent(HttpProxyEvent event);

	public void close() throws Exception;
}
