/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: StatusMonitor.java 
 *
 * @author yinqiwen [ 2010-5-21 | 07:40:09 PM]
 *
 */
package com.hyk.proxy.client.framework.status;

/**
 *
 */
public interface StatusMonitor
{
	public void clearStatusHistory();
	
	public void notifyStatus(String status);
	
	public void notifyRunDetail(String detail);
	
}
