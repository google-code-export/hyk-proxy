/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: FetchServiceImpl.java 
 *
 * @author yinqiwen [ Jan 25, 2010 | 2:37:31 PM ]
 *
 */
package com.hyk.proxy.gae.server.core.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.ResponseTooLargeException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.tools.appstats.AppstatsServlet;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.proxy.gae.common.http.SimpleNameValueListHeader;
import com.hyk.proxy.gae.common.service.FetchService;
import com.hyk.proxy.gae.server.util.ServerUtils;
import com.hyk.util.thread.ThreadLocalUtil;

/**
 *
 */
public class FetchServiceImpl implements FetchService
{
	protected Logger	logger	= LoggerFactory.getLogger(getClass());
	protected URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
	protected MemcacheService	memcache	= MemcacheServiceFactory.getMemcacheService();
	
	protected boolean cacheResponse(HttpRequestExchange req, HttpResponseExchange res)
	{
		//only 2xx for get could be cached
		if(res.getResponseCode() < 400 && req.getMethod().equalsIgnoreCase(HTTPMethod.GET.name()))
		{
			String cacheControlValue = res.getHeaderValue("cache-control");
			if(null != cacheControlValue)
			{
				SimpleNameValueListHeader cacheControl = new SimpleNameValueListHeader(cacheControlValue.trim());
				if(cacheControl.containsName("no-cache") || cacheControl.containsName("private"))
				{
					return false;
				}
				String maxAgeValue = cacheControl.getValue("max-age");
				if(null == maxAgeValue)
				{
					maxAgeValue = cacheControl.getValue("s-maxage");	
				}
				if(null != maxAgeValue)
				{
					int maxAge = Integer.parseInt(maxAgeValue);
					if(maxAge > 0)
					{
						memcache.put(req, res, Expiration.byDeltaSeconds(maxAge));
					}		
					return true;
				}
			}	
		}
		return false;
	}
	
	protected HttpResponseExchange getCache(HttpRequestExchange req)
	{
		if(req.getMethod().equalsIgnoreCase(HTTPMethod.GET.name()))
		{
			return (HttpResponseExchange)memcache.get(req);
		}
		return null;
	}
	
	public HttpResponseExchange fetch(HttpRequestExchange req)
	{
		HttpResponseExchange ret = null;
		try
		{			
			ret = getCache(req);
			if(null == ret)
			{
				HTTPRequest fetchReq = ServerUtils.toHTTPRequest(req);
				HTTPResponse fetchRes = urlFetchService.fetch(fetchReq);
				ret =  ServerUtils.toHttpResponseExchange(fetchRes);
				cacheResponse(req, ret);
			}
			
			String contentType = ret.getHeaderValue("content-type");
		    if(null == contentType)
		    {
		    	contentType = "";
		    }
			ThreadLocalUtil.getThreadLocalUtil(String.class).setThreadLocalObject(contentType);
			//AppstatsServlet.
		}
		catch(IOException e)
		{
			logger.error("Faile to fetch", e);
			if(e.getMessage().indexOf("Timeout") != -1)
			{
				ret = new HttpResponseExchange().setResponseTooLarge(true);
			}
			else
			{
				ret = new HttpResponseExchange();
				ret.setResponseCode(400);
			}
			ret = new HttpResponseExchange().setResponseTooLarge(true);
		}
		catch(ResponseTooLargeException e)
		{
			ret = new HttpResponseExchange().setResponseTooLarge(true);
		}
		catch(Exception e)
		{
			logger.error("Faile to fetch", e);
			ret = new HttpResponseExchange();
			ret.setResponseCode(400);
		}
		return ret;
	}

}
