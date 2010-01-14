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

/**
 *
 */
public class HttpResponseExchange extends HttpMessageExhange
{
	public int responseCode;
	
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
		
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		responseCode = in.readInt();
		super.readExternal(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(responseCode);
		super.writeExternal(out);
	}
	
	
}
