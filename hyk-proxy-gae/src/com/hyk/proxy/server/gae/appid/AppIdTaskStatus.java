/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppIdTaskStatus.java 
 *
 * @author yinqiwen [ 2010-8-30 | ÏÂÎç10:27:06 ]
 *
 */
package com.hyk.proxy.server.gae.appid;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 *
 */
@Cached
public class AppIdTaskStatus
{
	@Id
	private String appid;
	
	public String getAppid()
    {
    	return appid;
    }

	public void setAppid(String appid)
    {
    	this.appid = appid;
    }

	public boolean isFinished()
    {
    	return finished;
    }

	public void setFinished(boolean finished)
    {
    	this.finished = finished;
    }

	private boolean finished;
}
