/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpServerAddress.java 
 *
 * @author qiying.wang [ Jan 29, 2010 | 10:45:02 AM ]
 *
 */
package com.hyk.proxy.gae.client.http;

import com.hyk.rpc.core.address.Address;

/**
 *
 */
public class HttpServerAddress implements Address
{
	private String host;
	private int port;
	private String path;
	private boolean isSecure;
	
	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public String getPath()
	{
		return path;
	}


	
	public boolean isSecure()
	{
		return isSecure;
	}

	public String toPrintableString()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
