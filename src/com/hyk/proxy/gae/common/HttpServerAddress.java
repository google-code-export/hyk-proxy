/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpServerAddress.java 
 *
 * @author yinqiwen [ Jan 29, 2010 | 10:45:02 AM ]
 *
 */
package com.hyk.proxy.gae.common;

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
	
	public HttpServerAddress()
	{
		//do nothing
	}

	public HttpServerAddress(String host, String path, int port, boolean isSecure)
	{
		this.host = host;
		this.port = port;
		this.path = path;
		this.isSecure = isSecure;
	}

    public HttpServerAddress(String host, String path)
	{
		this(host, path, 80, false);
	}
    
    public HttpServerAddress(String host, int port, String path)
	{
		this(host, path, port, false);
	}
	
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
		return "http" + (isSecure?"s":"") + "//" + host  + ":" + port + "/" + path;
	}

}