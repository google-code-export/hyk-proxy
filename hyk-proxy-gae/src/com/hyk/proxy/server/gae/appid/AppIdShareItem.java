/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppIdShareItem.java 
 *
 * @author qiying.wang [ May 17, 2010 | 5:37:27 PM ]
 *
 */
package com.hyk.proxy.server.gae.appid;

import java.util.Set;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 *
 */
@Cached
public class AppIdShareItem
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

	public String getGmail()
	{
		return gmail;
	}

	public void setGmail(String gmail)
	{
		this.gmail = gmail;
	}

	private String gmail;
}
