/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: HttpResponseExchange.java 
 *
 * @author yinqiwen [ Jan 14, 2010 | 3:50:29 PM ]
 *
 */
package com.hyk.proxy.gae.common;

import java.io.IOException;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 *
 */
public class HttpResponseExchange extends HttpMessageExhange
{
	public int		responseCode;
	private String	redirectURL;
	private boolean isResponseTooLarge;

	public boolean isResponseTooLarge()
	{
		return isResponseTooLarge;
	}

	public HttpResponseExchange setResponseTooLarge(boolean isResponseTooLarge)
	{
		this.isResponseTooLarge = isResponseTooLarge;
		return this;
	}

	public void setRedirectURL(String redirectURL)
	{
		this.redirectURL = redirectURL;
	}

	public String getRedirectURL()
	{
		return redirectURL;
	}

	protected void print(StringBuffer buffer)
	{
		buffer.append(HttpResponseStatus.valueOf(responseCode)).append("\r\n");

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
		isResponseTooLarge = in.readBoolean();
		responseCode = in.readInt();
		if(in.readBoolean())
		{
			redirectURL = in.readString();
		}
		super.readExternal(in);
	}

	public void writeExternal(SerializerOutput out) throws IOException
	{
		out.writeBoolean(isResponseTooLarge);
		out.writeInt(responseCode);
		out.writeBoolean(null != redirectURL);
		if(null != redirectURL)
		{
			out.writeString(redirectURL);
		}
		super.writeExternal(out);
	}

}
