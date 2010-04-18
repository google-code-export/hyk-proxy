/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BandwidthStatisticsService.java 
 *
 * @author yinqiwen [ 2010-4-5 | обнГ07:06:15 ]
 *
 */
package com.hyk.proxy.gae.server.stat;



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
		
		 
	}
}
