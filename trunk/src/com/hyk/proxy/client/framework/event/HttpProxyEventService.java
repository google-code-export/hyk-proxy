/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyService.java 
 *
 * @author yinqiwen [ 2010-5-13 | обнГ07:50:44 ]
 *
 */
package com.hyk.proxy.client.framework.event;

/**
 *
 */
public interface HttpProxyEventService
{
	public void handleEvent(HttpProxyEvent event);
	
	public void close() throws Exception;
}
