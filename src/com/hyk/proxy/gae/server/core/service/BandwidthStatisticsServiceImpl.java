/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BandwidthStatisticsService.java 
 *
 * @author yinqiwen [ 2010-4-5 | ÏÂÎç07:06:15 ]
 *
 */
package com.hyk.proxy.gae.server.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.hyk.proxy.gae.common.service.BandwidthStatisticsService;
import com.hyk.proxy.gae.common.stat.BandwidthStatReport;
import com.hyk.proxy.gae.server.config.DatastoreConfig;
import com.hyk.proxy.gae.server.stat.BandwidthStatisticsResult;
import com.hyk.proxy.gae.server.util.ServerUtils;

/**
 *
 */
public class BandwidthStatisticsServiceImpl implements BandwidthStatisticsService
{
	private static final String CACHE_NAME = "__BANDWIDTH_STAT__";
	protected static MemcacheService	memcache		= MemcacheServiceFactory.getMemcacheService();
	
	protected boolean isEnable;
	
	public BandwidthStatisticsServiceImpl()
	{
		loadConfig();
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
	public BandwidthStatReport getStatResult(String host)
	{
		return ServerUtils.toBandwidthStatReport(ServerUtils.getBandwidthStatisticsResult(host));
	}

	@Override
	public BandwidthStatReport[] getStatResults(int limit)
	{
		List<BandwidthStatisticsResult> results = ServerUtils.getBandwidthStatisticsResults(limit);
		BandwidthStatReport[] ret = new BandwidthStatReport[results.size()];
		for(int i = 0; i < ret.length; i++)
		{
			ret[i] = ServerUtils.toBandwidthStatReport(results.get(i));
		}
		return ret;
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
		ServerUtils.deleteType(BandwidthStatisticsResult.class);
	}
}
