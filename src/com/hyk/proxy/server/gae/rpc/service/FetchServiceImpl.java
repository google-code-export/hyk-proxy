/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: FetchServiceImpl.java 
 *
 * @author yinqiwen [ Jan 25, 2010 | 2:37:31 PM ]
 *
 */
package com.hyk.proxy.server.gae.rpc.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import com.google.apphosting.api.ApiProxy.CapabilityDisabledException;
import com.hyk.proxy.common.gae.auth.Group;
import com.hyk.proxy.common.gae.auth.User;
import com.hyk.proxy.common.gae.stat.BandwidthStatisticsResult;
import com.hyk.proxy.common.http.header.SimpleNameValueListHeader;
import com.hyk.proxy.common.http.message.HttpRequestExchange;
import com.hyk.proxy.common.http.message.HttpResponseExchange;
import com.hyk.proxy.common.rpc.service.FetchService;
import com.hyk.proxy.server.gae.config.XmlConfig;
import com.hyk.proxy.server.gae.rpc.remote.Reloadable;
import com.hyk.proxy.server.gae.util.ServerUtils;
import com.hyk.util.thread.ThreadLocalUtil;

/**
 *
 */
public class FetchServiceImpl implements FetchService, Serializable, Reloadable
{

	protected transient Logger			logger;
	protected transient URLFetchService	urlFetchService;
	protected transient MemcacheService	memcache;

	Group								group;
	User								user;

	BandwidthStatisticsServiceImpl		bandwidthStatisticsService;

	Set<String>							blacklist	= new HashSet<String>();

	public FetchServiceImpl(Group group, User user, BandwidthStatisticsServiceImpl bandwidthStatisticsService)
	{
		init();
		setUserAndGroup(group, user);
		setBandwidthStatisticsService(bandwidthStatisticsService);
	}

	public void init()
	{
		urlFetchService = URLFetchServiceFactory.getURLFetchService();
		memcache = MemcacheServiceFactory.getMemcacheService();
		logger = LoggerFactory.getLogger(getClass());
	}

	public void setBandwidthStatisticsService(BandwidthStatisticsServiceImpl bandwidthStatisticsService)
	{
		this.bandwidthStatisticsService = bandwidthStatisticsService;
		bandwidthStatisticsService.loadConfig();
	}

	public void setUserAndGroup(Group group, User user)
	{
		this.group = group;
		this.user = user;
		blacklist.clear();
		if(null != group.getBlacklist())
		{
			blacklist.addAll(group.getBlacklist());
		}
		if(null != user.getBlacklist())
		{
			blacklist.addAll(user.getBlacklist());
		}
	}

	protected boolean cacheResponse(HttpRequestExchange req, HttpResponseExchange res)
	{
		// only 2xx/3xx for get could be cached
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
						try
						{
							memcache.put(req, res, Expiration.byDeltaSeconds(maxAge));
						}
						catch(CapabilityDisabledException e)
						{
							logger.error("In maintenance, memcache is not available!");
						}

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

	protected HttpResponseExchange createErrorResponse(String reason)
	{
		HttpResponseExchange res = new HttpResponseExchange();
		res.setResponseCode(403);
		res.addHeader("content-type", "text/html; charset=utf-8");

		String error = XmlConfig.getInstance().getAuthErrorPage();
		String content = String.format(error, reason);
		res.addHeader("content-length", "" + content.length());
		res.setBody(content.getBytes());
		return res;
	}

	protected boolean authByTrafficRestriction(HttpRequestExchange req)
	{
		Map<String, Integer> trafficRestrictionTable = user.getTrafficRestrictionTable();
		if(null == trafficRestrictionTable || trafficRestrictionTable.isEmpty())
		{
			return true;
		}
		if(null == req.getHeaderValue("Host"))
		{
			return true;
		}
		String host = req.getHeaderValue("Host").trim();
		String traficHost = host;
		if(!trafficRestrictionTable.containsKey(traficHost))
		{
			// match all hosts
			traficHost = "*";
		}
		if(!trafficRestrictionTable.containsKey(traficHost))
		{
			return true;
		}

		int restriction = trafficRestrictionTable.get(traficHost);
		if(restriction < 0) // no restriction
		{
			return true;
		}
		if(restriction == 0) // restrict all requests
		{
			return false;
		}
		BandwidthStatisticsResult result = bandwidthStatisticsService.getStatResult(host);
		if(null != result)
		{
			if(result.getIncoming() >= restriction || result.getOutgoing() >= restriction)
			{
				return false;
			}
		}

		return true;
	}

	protected boolean authByBlacklist(HttpRequestExchange req)
	{
		Iterator<String> hosts = blacklist.iterator();
		while(hosts.hasNext())
		{
			String host = hosts.next();
			if(host.equals("*"))
			{
				return false;
			}
			if(null == req.getHeaderValue("Host"))
			{
				return true;
			}
			if(req.getHeaderValue("Host").indexOf(host) != -1)
			{
				return false;
			}
		}
		return true;
	}

	public HttpResponseExchange fetch(HttpRequestExchange req)
	{
		HttpResponseExchange ret = null;
		try
		{
			if(!authByBlacklist(req))
			{
				return createErrorResponse("blacklist restriction");
			}
			if(!authByTrafficRestriction(req))
			{
				return createErrorResponse("your account:" + user.getEmail() + " has exceed the traffic limit today for the proxyed host:"
						+ req.getHeaderValue("Host"));
			}
			ret = getCache(req);
			if(null == ret)
			{
				HTTPRequest fetchReq = ServerUtils.toHTTPRequest(req);
				HTTPResponse fetchRes = urlFetchService.fetch(fetchReq);
				ret = ServerUtils.toHttpResponseExchange(fetchRes);
				cacheResponse(req, ret);
			}

			String contentType = ret.getHeaderValue("content-type");
			if(null == contentType)
			{
				contentType = "";
			}
			// Store this value since the RPC framework would use this value to
			// judge whole message compressing or not
			ThreadLocalUtil.getThreadLocalUtil(String.class).setThreadLocalObject(contentType);
			if(bandwidthStatisticsService.isEnable() && null != req.getHeaderValue("Host"))
			{
				int reqBodyLen = req.getContentLength();
				int resBodyLen = ret.getContentLength();
				String host = req.getHeaderValue("Host");
				bandwidthStatisticsService.statBandwidth(host, reqBodyLen, resBodyLen);
			}
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
		catch(Throwable e)
		{
			logger.error("Faile to fetch", e);
			ret = new HttpResponseExchange();
			ret.setResponseCode(400);
		}
		return ret;
	}

}
