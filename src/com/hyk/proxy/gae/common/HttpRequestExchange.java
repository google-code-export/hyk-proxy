/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: HttpRequestExchange.java 
 *
 * @author qiying.wang [ Jan 14, 2010 | 3:49:21 PM ]
 *
 */
package com.hyk.proxy.gae.common;

import java.io.IOException;

import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 *
 */
public class HttpRequestExchange extends HttpMessageExhange
{
	public String url;
	public String method;
	
	public String getMethod()
	{
		return method;
	}

	public void setURL(String url)
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
	protected void print() {
		System.out.print(method);
		System.out.print("  ");
		System.out.println(url);
		
	}
}
