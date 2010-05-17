/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BandwidthStatisticsService.java 
 *
 * @author yinqiwen [ 2010-4-5 | ÏÂÎç07:06:15 ]
 *
 */
package com.hyk.proxy.server.gae.rpc.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.hyk.proxy.common.gae.stat.BandwidthStatisticsResult;
import com.hyk.proxy.common.rpc.service.BandwidthStatisticsService;
import com.hyk.proxy.server.gae.config.DatastoreConfig;
import com.hyk.proxy.server.gae.util.ServerUtils;


/**
 *
 */
public class BandwidthStatisticsServiceImpl implements BandwidthStatisticsService, Serializable
{
	private transient static final String CACHE_NAME = "__BANDWIDTH_STAT__";
	protected transient static MemcacheService	memcache		= MemcacheServiceFactory.getMemcacheService();
	
	protected boolean isEnable;
	
	public BandwidthStatisticsServiceImpl()
	{
		loadConfig();
	}
	
	public void init()
	{
		
	}
	
	public void statBandwidth(String host, long incoming, long outgoing)
	{
		Map<String, BandwidthStatisticsResult> stats = (Map<String, BandwidthStatisticsResult>)memcache.get(CACHE_NAME);
		if(null == stats)
		{
			stats = new HashMap<String, BandwidthStatisticsResult>();
		}
		BandwidthStatisticsResult stat = stats.get(host);
		if(null == stat)
		{
			stat = new BandwidthStatisticsResult(host, incoming, outgoing);
			stats.put(host, stat);
		}
		else
		{
			stat.setIncoming(stat.getIncoming() + incoming);
			stat.setOutgoing(stat.getOutgoing() + outgoing);
		}
		memcache.put(CACHE_NAME, stats);
	}
	
	public static void storeStatResults()
	{
		Map<String, BandwidthStatisticsResult> stats = (Map<String, BandwidthStatisticsResult>)memcache.get(CACHE_NAME);
		if(null != stats && !stats.isEmpty())
		{
			List<BandwidthStatisticsResult> stores = new ArrayList<BandwidthStatisticsResult>();
			for(BandwidthStatisticsResult result:stats.values())
			{
				BandwidthStatisticsResult store = ServerUtils.getBandwidthStatisticsResult(result.getTargetSiteHost());
				if(null == store)
				{
					store = result;
				}
				else
				{
					store.setIncoming(store.getIncoming() + result.getIncoming());
					store.setOutgoing(store.getOutgoing() + result.getOutgoing());
				}
				stores.add(store);
			}
			if(!stores.isEmpty())
			{
				ServerUtils.storeObjects(stores);
			}
			memcache.delete(CACHE_NAME);
		}
		//loadConfig();
	}
	
	public void loadConfig()
	{
		isEnable = ServerUtils.getDatastoreConfig().isStatEnable();
	}

	@Override
	public BandwidthStatisticsResult getStatResult(String host)
	{
		return ServerUtils.getBandwidthStatisticsResult(host);
	}

	@Override
	public List<BandwidthStatisticsResult> getStatResults(int limit)
	{
		List<BandwidthStatisticsResult> results = ServerUtils.getBandwidthStatisticsResults(limit);
		return ServerUtils.getBandwidthStatisticsResults(limit);
	}

	@Override
	public void enable(boolean flag)
	{
		isEnable = flag;
		DatastoreConfig  config = ServerUtils.getDatastoreConfig();
		if(config.setStatEnable(flag))
		{
			ServerUtils.storeObject(config);
		}
	}

	@Override
	public boolean isEnable()
	{
		return isEnable;
	}

	@Override
	public void clear()
	{
		clearRecord();
	}
	
	public static void clearRecord()
	{
		memcache.delete(CACHE_NAME);
		if(!ServerUtils.deleteTypeWithLimit(BandwidthStatisticsResult.class, 1000))
		{
			Queue queue = QueueFactory.getDefaultQueue();
			TaskOptions task = TaskOptions.Builder.url("/clear-stat-records").method(Method.GET);
			queue.add(task);
		}
		//ServerUtils.deleteType(BandwidthStatisticsResult.class);
	}
}
