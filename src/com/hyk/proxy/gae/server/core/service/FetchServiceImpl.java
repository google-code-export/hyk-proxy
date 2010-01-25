/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: FetchServiceImpl.java 
 *
 * @author qiying.wang [ Jan 25, 2010 | 2:37:31 PM ]
 *
 */
package com.hyk.proxy.gae.server.core.service;

import java.io.IOException;

import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;

/**
 *
 */
public class FetchServiceImpl implements FetchService
{
	public HttpResponseExchange fetch(HttpRequestExchange req)
	{

		try
		{
			HTTPRequest fetchReq = req.toHTTPRequest();
			HTTPResponse fetchRes = URLFetchServiceFactory.getURLFetchService()
					.fetch(fetchReq);
			return new HttpResponseExchange(fetchRes);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
