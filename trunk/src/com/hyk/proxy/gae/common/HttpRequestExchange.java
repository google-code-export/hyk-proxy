/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: HttpRequestExchange.java 
 *
 * @author yinqiwen [ Jan 14, 2010 | 3:49:21 PM ]
 *
 */
package com.hyk.proxy.gae.common;

import java.io.IOException;
import java.util.Arrays;

import com.hyk.compress.CompressorPreference;
import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 *
 */
public class HttpRequestExchange extends HttpMessageExhange
{
	public String	url;
	
	public String	method;

	public HttpRequestExchange clone()
	{
		HttpRequestExchange ret = new HttpRequestExchange();
		ret.url = url;
		ret.method = method;
		ret.headers = getCloneHeaders();
		ret.body = body;
		return ret;
	}
	
	@Override
	public int hashCode()
	{
		int hash = 0;
		hash += url.hashCode();
		hash += method.hashCode();
		hash += super.hashCode();
		return hash;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(null == obj)
		{
			return false;
		}
		if(obj instanceof HttpRequestExchange)
		{
			HttpRequestExchange other = (HttpRequestExchange)obj;
			return url.equals(other.url) && method.equals(other.method) && super.equals(obj);
		}
		return false;
	}
	
	public String getMethod()
	{
		return method;
	}

	public String getUrl() 
	{
		return url;
	}
	
	public void setUrl(String url)
	{
		this.url = url;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}
	

	public void readExternal(SerializerInput in) throws IOException
	{
		url = in.readString();
		method = in.readString();
		super.readExternal(in);
	}

	public void writeExternal(SerializerOutput out) throws IOException
	{
		out.writeString(url);
		out.writeString(method);
		super.writeExternal(out);
	}

	@Override
	protected void print(StringBuffer buffer)
	{
		buffer.append(method).append("  ").append(url).append("\r\n");
	}
}
