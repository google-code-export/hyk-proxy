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
import java.util.List;

import javax.jdo.PersistenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.hyk.proxy.gae.common.http.message.HttpRequestExchange;
import com.hyk.proxy.gae.common.http.message.HttpResponseExchange;
import com.hyk.proxy.gae.server.account.Group;
import com.hyk.proxy.gae.server.account.User;
import com.hyk.proxy.gae.server.remote.RemoteObject;
import com.hyk.proxy.gae.server.remote.RemoteObjectId;
import com.hyk.proxy.gae.server.remote.RemoteObjectType;

/**
 *
 */
public class ServerUtils
{
	protected static Logger					logger				= LoggerFactory.getLogger(ServerUtils.class);
	
	protected static MemcacheService	memcache	= MemcacheServiceFactory.getMemcacheService();

	protected static Objectify			ofy			= ObjectifyService.begin();

	static
	{
		ObjectifyService.register(User.class);
		ObjectifyService.register(Group.class);
		ObjectifyService.register(RemoteObject.class);
		ObjectifyService.register(RemoteObjectId.class);
	}
	
	public static long storeObject(Object obj)
	{
		Key key = ofy.put(obj);
		return key.getId();
	}

	public static void deleteObject(Object obj)
	{
		ofy.delete(obj);
	}

	public static List<Group> getAllGroups()
	{
		QueryResultIterable<Group> results = ofy.query(Group.class).fetch();
		List<Group> groups = new ArrayList<Group>();
		for(Group group : results)
		{
			groups.add(group);
		}
		return groups;
	}

	public static List<User> getAllUsers()
	{
		QueryResultIterable<User> results = ofy.query(User.class).fetch();
		List<User> users = new ArrayList<User>();
		for(User user : results)
		{
			users.add(user);
		}
		return users;
	}

	public static void storeObject(PersistenceManager pm, Object obj)
	{
		pm.makePersistent(obj);
	}

	public static void saveRemoteObject(long objId, RemoteObjectType type, String username, String groupname)
	{
		RemoteObject ro = new RemoteObject();
		ro.setType(type);
		ro.setUsername(username);
		ro.setGroupname(groupname);
		ro.setObjid(objId);
		storeObject(ro);
        
	}

	public static List<RemoteObject> loadRemoteObjects()
	{
		QueryResultIterable<RemoteObject> results = ofy.query(RemoteObject.class).fetch();
		List<RemoteObject> ret = new ArrayList<RemoteObject>();
		for(RemoteObject ro : results)
		{
			ret.add(ro);
		}
		return ret;
	}

	public static Group getGroup(String name)
	{
		Group group = ofy.find(Group.class, name);
		return group;
	}

	public static User getUser(String email)
	{
		return ofy.find(User.class, email);
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
