/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Util.java 
 *
 * @author yinqiwen [ 2010-1-30 | pm09:53:28 ]
 *
 */
package com.hyk.proxy.gae.server.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.hyk.proxy.gae.common.http.message.HttpRequestExchange;
import com.hyk.proxy.gae.common.http.message.HttpResponseExchange;

/**
 *
 */
public class ServerUtils
{
	
	private static long getContentLength(List<HTTPHeader> headers )
	{
		for(HTTPHeader header:headers)
		{
			if(header.getName().equalsIgnoreCase("content-length"))
			{
				String length = header.getValue().trim();
				return Long.parseLong(length);
			}
		}
		return 0;
	}
	
	public static long getContentLength(HTTPRequest request)
	{
		List<HTTPHeader> headers = request.getHeaders();
		return getContentLength(headers);
	}
	
	public static long getContentLength(HTTPResponse response)
	{
		List<HTTPHeader> headers = response.getHeaders();
		return getContentLength(headers);
	}
	
	public static String getContentType(HTTPResponse response)
	{
		List<HTTPHeader> headers = response.getHeaders();
		return getContentType(headers);
	}
	
	public static String getContentType(List<HTTPHeader> headers)
	{
		for(HTTPHeader header:headers)
		{
			if(header.getName().equalsIgnoreCase("content-type"))
			{
				return header.getValue().trim();
			}
		}
		return null;
	}
	
	public static HTTPRequest toHTTPRequest(HttpRequestExchange exchange) throws MalformedURLException
	{
		URL requrl = new URL(exchange.url);
		FetchOptions fetchOptions = FetchOptions.Builder.withDeadline(10).disallowTruncate().doNotFollowRedirects();
		HTTPRequest req = new HTTPRequest(requrl,HTTPMethod.valueOf(exchange.method), fetchOptions);
		for(String[] header:exchange.getHeaders())
		{
			req.addHeader(new HTTPHeader(header[0], header[1]));
		}
		req.setPayload(exchange.getBody().toByteArray());
		return req;
	}
	
	public static HttpResponseExchange toHttpResponseExchange(HTTPResponse res)
	{
		HttpResponseExchange exchange = new HttpResponseExchange();
		exchange.setResponseCode(res.getResponseCode());
		List<HTTPHeader> headers = res.getHeaders();
		for(HTTPHeader header : headers)
		{
			exchange.addHeader(header.getName(), header.getValue());
		}
		exchange.setBody(res.getContent());
		URL url = res.getFinalUrl();
		if(null != url)
		{
			exchange.setRedirectURL(url.toString());
		}
		return exchange;
	}
}
