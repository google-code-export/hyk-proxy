/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ServerConfigurationService.java 
 *
 * @author yinqiwen [ 2011-12-3 | ÏÂÎç03:14:28 ]
 *
 */
package org.hyk.proxy.gae.server.service;

import java.util.HashSet;
import java.util.Set;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.hyk.proxy.gae.common.CompressorType;
import org.hyk.proxy.gae.common.EncryptType;
import org.hyk.proxy.gae.common.config.GAEServerConfiguration;
import org.hyk.proxy.gae.common.event.AdminResponseEvent;
import org.hyk.proxy.gae.common.event.ServerConfigEvent;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 *
 */
public class ServerConfigurationService
{
	private static DatastoreService datastore = DatastoreServiceFactory
	        .getDatastoreService();
	private static AsyncDatastoreService asyncdatastore = DatastoreServiceFactory
	        .getAsyncDatastoreService();
	protected static AsyncMemcacheService asyncCache = MemcacheServiceFactory
	        .getAsyncMemcacheService();
	protected static MemcacheService cache = MemcacheServiceFactory
	        .getMemcacheService();
	private static GAEServerConfiguration cfg;

	private static Entity toEntity(GAEServerConfiguration cfg)
	{
		Entity entity = new Entity("ServerConfig", 1);
		entity.setProperty("FetchRetryCount", "" + cfg.getFetchRetryCount());
		entity.setProperty("MaxXMPPDataPackageSize", "" + cfg.getMaxXMPPDataPackageSize());
		entity.setProperty("RangeFetchLimit", "" + cfg.getRangeFetchLimit());
		entity.setProperty("Compressor", "" + cfg.getCompressor().toString());
		entity.setProperty("Encrypter", "" + cfg.getEncrypter().toString());
		//entity.setProperty("TrafficStatEnable", "" + cfg.isTrafficStatEnable());
		Set<String> set = cfg.getCompressFilter();
		StringBuilder buffer = new StringBuilder();
		if(null != set)
		{
			for(String s:set)
			{
				if(!s.isEmpty())
				{
					buffer.append(s).append(";");
				}
			}
		}
		entity.setProperty("CompressFilter", buffer.toString());
		return entity;
	}
	
	private static GAEServerConfiguration fromEntity(Entity entity)
	{
		GAEServerConfiguration cfg = new GAEServerConfiguration();
		cfg.setFetchRetryCount(Integer.parseInt((String) entity.getProperty("FetchRetryCount")));
		cfg.setMaxXMPPDataPackageSize(Integer.parseInt((String) entity.getProperty("MaxXMPPDataPackageSize")));
		cfg.setRangeFetchLimit(Integer.parseInt((String) entity.getProperty("RangeFetchLimit")));
		cfg.setCompressor(CompressorType.valueOf((String) entity.getProperty("Compressor")));
		cfg.setEncrypter(EncryptType.valueOf((String) entity.getProperty("Encrypter")));
		//cfg.setTrafficStatEnable(Boolean.parseBoolean((String) entity.getProperty("TrafficStatEnable")));
		String str = (String) entity.getProperty("CompressFilter");
		if(null != str)
		{
			String[] ss = str.split(";");
			Set<String> set  = new HashSet<String>();
			for(String s:ss)
			{
				s = s.trim();
				if(!s.isEmpty())
				{
					set.add(s);
				}
			}
			cfg.setCompressFilter(set);
		}
		return cfg;
	}
	
	public static GAEServerConfiguration getServerConfig()
	{
		if(null == cfg)
		{
			byte[] content = (byte[]) cache.get("ServerConfig:");
			if(null != content)
			{
				Buffer buf = Buffer.wrapReadableContent(content);
				cfg = new GAEServerConfiguration();
				cfg.decode(buf);
			}
			else
			{
				Key key = KeyFactory.createKey("ServerConfig", 1);
				try
                {
	                Entity entity = datastore.get(key);
	                cfg = fromEntity(entity);
	                Buffer buffer = new Buffer(256);
	                cfg.encode(buffer);
	                asyncCache.put("ServerConfig:", buffer.toArray());
                }
                catch (EntityNotFoundException e)
                {
                	saveServerConfig(new GAEServerConfiguration());    	
                }
			}
			
		}
		return cfg;
	}
	
	public static Event handleServerConfig(ServerConfigEvent event)
	{
		switch (event.opreration)
        {
	        case ServerConfigEvent.GET_CONFIG_REQ:
	        {   
	        	GAEServerConfiguration cfg = getServerConfig();
	        	ServerConfigEvent res = new ServerConfigEvent();
	        	res.opreration = ServerConfigEvent.GET_CONFIG_RES;
	        	res.cfg = cfg;
		        return res;
	        }
	        case ServerConfigEvent.SET_CONFIG_REQ:
	        {
	        	saveServerConfig(event.cfg);
	        	ServerConfigEvent res = new ServerConfigEvent();
	        	res.opreration = ServerConfigEvent.SET_CONFIG_RES;
	        	res.cfg = getServerConfig();
		        return res;
	        }
	        default:
	        {
		        return new AdminResponseEvent("", "Unsupported config operation:" + event.opreration, 0);
	        }
        }
	}

	private static void saveServerConfig(GAEServerConfiguration cfg)
	{
		ServerConfigurationService.cfg = cfg;
		Buffer buf = new Buffer(256);
    	cfg.encode(buf);
    	asyncCache.put("ServerConfig:", buf.toArray());  
    	Key key = KeyFactory.createKey("ServerConfig", 1);
    	Entity entity = toEntity(cfg);
    	asyncdatastore.put(entity);
	}

}
