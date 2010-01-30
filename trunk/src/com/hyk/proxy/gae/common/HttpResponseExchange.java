/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: HttpResponseExchange.java 
 *
 * @author qiying.wang [ Jan 14, 2010 | 3:50:29 PM ]
 *
 */
package com.hyk.proxy.gae.common;

import java.io.IOException;

import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 *
 */
public class HttpResponseExchange extends HttpMessageExhange
{
	public int		responseCode;
	private String	redirectURL;

	public void setRedirectURL(String redirectURL)
	{
		this.redirectURL = redirectURL;
	}

	public String getRedirectURL()
	{
		return redirectURL;
	}

	protected void print()
	{

		System.out.println(responseCode);

	}

	public void setResponseCode(int responseCode)
	{
		this.responseCode = responseCode;
	}

	public int getResponseCode()
	{
		return this.responseCode;
	}

	public HttpResponseExchange()
	{
	}

	public void readExternal(SerializerInput in) throws IOException
	{
		responseCode = in.readInt();
		if(in.readBoolean())
		{
			redirectURL = in.readString();
		}
		super.readExternal(in);
	}

	public void writeExternal(SerializerOutput out) throws IOException
	{
		out.writeInt(responseCode);
		out.writeBoolean(null != redirectURL);
		if(null != redirectURL)
		{
			out.writeString(redirectURL);
		}
		super.writeExternal(out);
	}

}
