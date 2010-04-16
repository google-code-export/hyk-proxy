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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.apphosting.api.ApiProxy.CapabilityDisabledException;
import com.hyk.proxy.gae.common.http.message.HttpRequestExchange;
import com.hyk.proxy.gae.common.http.message.HttpResponseExchange;
import com.hyk.proxy.gae.server.account.Group;
import com.hyk.proxy.gae.server.account.User;
import com.hyk.proxy.gae.server.remote.RemoteObject;
import com.hyk.proxy.gae.server.remote.RemoteObjectType;

/**
 *
 */
public class ServerUtils
{
	protected static MemcacheService	memcache	= MemcacheServiceFactory.getMemcacheService();
	
	public static void removeUserCache(User u)
	{
		memcache.delete(User.CACHE_NAME + u.getEmail());
	}
	
	public static void removegroupCache(Group g)
	{
		memcache.delete(Group.CACHE_NAME + g.getName());
	}
	
	public static void cacheUser(User u)
	{
		User cache = new User();
		cache.setEmail(u.getEmail());
		cache.setGroup(u.getGroup());
		cache.setPasswd(u.getPasswd());
		Set<String> blacklist = new HashSet<String>();
		blacklist.addAll(u.getBlacklist());
		cache.setBlacklist(blacklist);
		memcache.put(User.CACHE_NAME + u.getEmail(), cache);
	}
	
	public static void cacheGroup(Group g)
	{
		Group cache = new Group();
		cache.setName(g.getName());
		Set<String> blacklist = new HashSet<String>();
		blacklist.addAll(g.getBlacklist());
		cache.setBlacklist(blacklist);
		memcache.put(Group.CACHE_NAME + g.getName(), cache);
	}
	
	public static void storeObject(Object obj)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try
		{
			pm.makePersistent(obj);
		}
		finally
		{
			pm.close();
		}
	}

	public static void storeObject(PersistenceManager pm, Object obj)
	{
		pm.makePersistent(obj);
	}
	
	public static void saveRemoteObject(long objid, RemoteObjectType type, String username,  String groupname)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try
		{
			Query query = pm.newQuery(RemoteObject.class);
			query.setFilter("objid == \"" + objid + "\"");
			List<RemoteObject> groupResults = (List<RemoteObject>)query.execute();
			// Group rootGroup = pm.getObjectById(Group.class, ROOT_GROUP_NAME);
			if(null != groupResults &&  !groupResults.isEmpty())
			{
				return;
			}
			RemoteObject ro = new RemoteObject();
			ro.setObjid(objid);
			ro.setType(type);
			ro.setUsername(username);
			ro.setGroupname(groupname);
			pm.makePersistent(ro);
			List<RemoteObject> ros = (List<RemoteObject>)memcache.get(RemoteObject.CACHE_LIST_NAME);
			if(null == ros)
			{
				ros = new ArrayList<RemoteObject>();
			}
			ros.add(ro);
			memcache.put(RemoteObject.CACHE_LIST_NAME, ros);
		}
		finally
		{
			pm.close();
		}
	}
	
	public static List<RemoteObject> loadRemoteObjects(PersistenceManager pm)
	{
		List<RemoteObject> ros = (List<RemoteObject>)memcache.get(RemoteObject.CACHE_LIST_NAME);
		if(null != ros)
		{
			return ros;
		}
		Query query = pm.newQuery(RemoteObject.class);
		// query.setFilter("objid == \"" + objid + "\"");
		List<RemoteObject> results = (List<RemoteObject>)query.execute();
		List<RemoteObject> cache = new ArrayList<RemoteObject>();
		cache.addAll(results);
		try
		{
			memcache.put(RemoteObject.CACHE_LIST_NAME, cache);
		}
		catch(CapabilityDisabledException e)
		{
			//do nothing;
		}
		
		return cache;
	}

	public static User getUser(PersistenceManager pm, String name)
	{
		Query query = pm.newQuery(User.class);
		query.setFilter("email == \"" + name + "\"");
		List<User> results = (List<User>)query.execute();
		if(!results.isEmpty())
		{
			return results.get(0);
		}
		return null;
	}

	public static Group getGroup(PersistenceManager pm, String name)
	{
		Query query = pm.newQuery(Group.class);
		query.setFilter("name == \"" + name + "\"");
		List<Group> groupResults = (List<Group>)query.execute();
		// Group rootGroup = pm.getObjectById(Group.class, ROOT_GROUP_NAME);
		if(null == groupResults || groupResults.isEmpty())
		{
			return null;
		}
		return groupResults.get(0);

	}

	public static Group getGroup(String name)
	{
		Group group = (Group)memcache.get(Group.CACHE_NAME + name);
		if(null == group)
		{
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try
			{
				Query query = pm.newQuery(Group.class);
				query.setFilter("name == \"" + name + "\"");
				List<Group> groupResults = (List<Group>)query.execute();
				// Group rootGroup = pm.getObjectById(Group.class, ROOT_GROUP_NAME);
				if(null == groupResults || groupResults.isEmpty())
				{
					return null;
				}
				group =  groupResults.get(0);
				cacheGroup(group);
			}
			finally
			{
				pm.close();
			}
		}
		return group;
		
	}

	public static User getUser(String email)
	{
		User user = (User)memcache.get(User.CACHE_NAME + email);
		if(null == user)
		{
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try
			{
				Query query = pm.newQuery(User.class);
				query.setFilter("email == \"" + email + "\"");
				List<User> userResults = (List<User>)query.execute();
				if(null == userResults || userResults.isEmpty())
				{
					return null;
				}
				user =  userResults.get(0);
				cacheUser(user);
			}
			finally
			{
				pm.close();
			}
		}
		return user;
		
	}

	private static long getContentLength(List<HTTPHeader> headers)
	{
		for(HTTPHeader header : headers)
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
		for(HTTPHeader header : headers)
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
		// HTTPMethod.
		FetchOptions fetchOptions = FetchOptions.Builder.withDeadline(10).disallowTruncate().doNotFollowRedirects();
		HTTPRequest req = new HTTPRequest(requrl, HTTPMethod.valueOf(exchange.method), fetchOptions);
		for(String[] header : exchange.getHeaders())
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

	public static String toString(Object obj)
	{
		if(null == obj)
		{
			return "null";
		}
		return obj.toString();
	}
}
