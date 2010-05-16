/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: DatastoreConfig.java 
 *
 * @author qiying.wang [ Apr 19, 2010 | 1:36:53 PM ]
 *
 */
package com.hyk.proxy.server.gae.config;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 *
 */
@Cached()
public class DatastoreConfig
{
	public static final long ID = 48100;
	
	@Id
    private Long id = ID;
	
	private boolean isStatEnable;

	public boolean isStatEnable()
	{
		return isStatEnable;
	}

	public boolean setStatEnable(boolean isStatEnable)
	{
		if(this.isStatEnable == isStatEnable)
		{
			return false;
		}
		this.isStatEnable = isStatEnable;
		return true;
	}
	
	
}
