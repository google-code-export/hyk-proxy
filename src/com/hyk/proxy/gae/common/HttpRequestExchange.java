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
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;
import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 *
 */
public class HttpRequestExchange extends HttpMessageExhange
{
	public String url;
	public String method;
	
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
	
	
	public HTTPRequest toHTTPRequest() throws MalformedURLException
	{
		//if()
		URL requrl = new URL(url);
		HTTPRequest req = new HTTPRequest(requrl,HTTPMethod.valueOf(method), FetchOptions.Builder.disallowTruncate().doNotFollowRedirects());
		for(String[] header:headers)
		{
			req.addHeader(new HTTPHeader(header[0], header[1]));
		}
		req.setPayload(body);
		return req;
	}

	@Override
	protected void print() {
		System.out.print(method);
		System.out.print("  ");
		System.out.println(url);
		
	}
}
