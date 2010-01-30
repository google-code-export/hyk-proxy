/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Util.java 
 *
 * @author Administrator [ 2010-1-30 | pm09:53:28 ]
 *
 */
package com.hyk.proxy.gae.server.core.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;

/**
 *
 */
public class Util
{
	public static HTTPRequest toHTTPRequest(HttpRequestExchange exchange) throws MalformedURLException
	{
		//if()
		URL requrl = new URL(exchange.url);
		HTTPRequest req = new HTTPRequest(requrl,HTTPMethod.valueOf(exchange.method), FetchOptions.Builder.disallowTruncate().doNotFollowRedirects());
		for(String[] header:exchange.getHeaders())
		{
			req.addHeader(new HTTPHeader(header[0], header[1]));
		}
		req.setPayload(exchange.getBody());
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
