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

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;

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
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		url = in.readUTF();
		method = in.readUTF();
		super.readExternal(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeUTF(url);
		out.writeUTF(method);
		super.writeExternal(out);
	}
	
	
	public HTTPRequest toHTTPRequest() throws MalformedURLException
	{
		URL requrl = new URL(url);
		HTTPRequest req = new HTTPRequest(requrl,HTTPMethod.valueOf(method));
		for(String[] header:headers)
		{
			req.addHeader(new HTTPHeader(header[0], header[1]));
		}
		req.setPayload(body);
		return req;
	}
	
	public static void main(String[] args) throws NotSerializableException, IOException, InstantiationException
	{
		HykSerializer serializer = new HykSerializer();
		HttpRequestExchange req = new HttpRequestExchange();
		req.setBody("564994590@!#@!#!@$".getBytes());
		req.setMethod("GET");
		req.addHeader("asdasf", "sadsaa");
		req.addHeader("sadasdas", "sadsafsaf");
		req.setURL("http://twitter.com/fzhenghu");
		byte[] data = serializer.serialize(req);
		System.out.println("####data len " + data.length);
		req = serializer.deserialize(HttpRequestExchange.class, data);
	}
}
