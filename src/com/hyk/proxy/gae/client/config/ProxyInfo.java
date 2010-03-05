/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ProxyInfo.java 
 *
 * @author yinqiwen [ 2010-3-5 | 07:15:59 PM ]
 *
 */
package com.hyk.proxy.gae.client.config;

/**
 *
 */
public class ProxyInfo
{
	public String getHost()
	{
		return host;
	}
	public void setHost(String host)
	{
		this.host = host;
	}
	public int getPort()
	{
		return port;
	}
	public void setPort(int port)
	{
		this.port = port;
	}
	public String getUser()
	{
		return user;
	}
	public void setUser(String user)
	{
		this.user = user;
	}
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	private String host;
	private int port = 80;
	private String user;
	private String password;
	
}
