/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppIdAuth.java 
 *
 * @author yinqiwen [ 2010-4-10 | ÉÏÎç10:32:39 ]
 *
 */
package com.hyk.proxy.gae.client.config;

/**
 *
 */
public class AppIdAuth
{
	public String getAppid()
	{
		return appid;
	}
	public void setAppid(String appid)
	{
		this.appid = appid;
	}
	public String getEmail()
	{
		return email;
	}
	public void setEmail(String email)
	{
		this.email = email;
	}
	public String getPasswd()
	{
		return passwd;
	}
	public void setPasswd(String passwd)
	{
		this.passwd = passwd;
	}
	private String appid;
	private String email;
	private String passwd;
	
}
