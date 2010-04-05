/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BandwidthStatisticsService.java 
 *
 * @author yinqiwen [ 2010-4-5 | ÏÂÎç07:06:15 ]
 *
 */
package com.hyk.proxy.gae.server.stat;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.hyk.proxy.gae.server.util.PMF;

/**
 *
 */
public class BandwidthStatisticsService
{
	private static BandwidthStatisticsService instance = new BandwidthStatisticsService();
	
	private BandwidthStatisticsService()
	{	
		//
	}
	
	public static BandwidthStatisticsService getInstance()
	{
		return instance;
	}
	
	public void statBandwidth(String host, long incoming, long outgoing)
	{
		 PersistenceManager pm = PMF.get().getPersistenceManager();
		 BandwidthStatisticsResult statisticsResult = pm.getObjectById(BandwidthStatisticsResult.class, host);
		try
		{
			if(null != statisticsResult)
			 {
				statisticsResult.setIncoming(statisticsResult.getIncoming() + incoming);
				statisticsResult.setOutgoing(statisticsResult.getOutgoing() + outgoing);
			 }
			 else
			 {
				 statisticsResult = new BandwidthStatisticsResult();
				 statisticsResult.setTargetSiteHost(host);
				 statisticsResult.setIncoming(incoming);
				 statisticsResult.setOutgoing(outgoing);
				 pm.makePersistent(statisticsResult);
			 }
		}
		finally
		{
			pm.close();
		}
		 
	}
}
