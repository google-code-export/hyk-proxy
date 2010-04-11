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

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.hyk.proxy.gae.common.http.message.HttpRequestExchange;
import com.hyk.proxy.gae.common.http.message.HttpResponseExchange;
import com.hyk.proxy.gae.server.account.Group;
import com.hyk.proxy.gae.server.account.User;
import com.hyk.proxy.gae.server.remote.RemoteObject;
import com.hyk.proxy.gae.server.remote.RemoteObjectType;
import com.hyk.util.random.RandomUtil;

/**
 *
 */
public class ServerUtils
{

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
		}
		finally
		{
			pm.close();
		}
	}
	
	public static List<RemoteObject> loadRemoteObjects(PersistenceManager pm)
	{
		Query query = pm.newQuery(RemoteObject.class);
		// query.setFilter("objid == \"" + objid + "\"");
		List<RemoteObject> results = (List<RemoteObject>)query.execute();
		return results;

	}

	public static User getUser(PersistenceManager pm, String name)
	{
		Query query = pm.newQuery(User.class);
		query.setFilter("email == \"" + name + "\"");
		List<User> results = (List<User>)query.execute();
		return (results == null || results.isEmpty()) ? null : results.get(0);
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
			return groupResults.get(0);
		}
		finally
		{
			pm.close();
		}
	}

	public static User getUser(String email)
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
			return userResults.get(0);
		}
		finally
		{
			pm.close();
		}
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
