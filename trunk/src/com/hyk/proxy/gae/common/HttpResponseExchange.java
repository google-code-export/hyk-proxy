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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 *
 */
public class HttpResponseExchange extends HttpMessageExhange
{
	public int responseCode;
	private String redirectURL;
	
	public String getRedirectURL() {
		return redirectURL;
	}
	
	protected void print() {

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
	
	public HttpResponseExchange(){}
	
	public HttpResponseExchange(HTTPResponse res)
	{
		setResponseCode(res.getResponseCode());
		List<HTTPHeader> headers = res.getHeaders();
		for(HTTPHeader header : headers)
		{
			addHeader(header.getName(), header.getValue());
		}
		setBody(res.getContent());
		URL url = res.getFinalUrl();
		if(null != url)
		{
			redirectURL = url.toString();
		}
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
