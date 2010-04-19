/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BandwidthStatisticsService.java 
 *
 * @author yinqiwen [ 2010-4-5 | ÏÂÎç07:06:15 ]
 *
 */
package com.hyk.proxy.gae.common.service;


import com.hyk.proxy.gae.common.stat.BandwidthStatReport;
import com.hyk.rpc.core.annotation.Remote;



/**
 *
 */
@Remote
public interface BandwidthStatisticsService
{	
	public BandwidthStatReport getStatResult(String host);
	public BandwidthStatReport[] getStatResults();
	
	//public void statBandwidth(String host, long incoming, long outgoing);
	public void clear();
	public void enable(boolean flag);
	public boolean isEnable();
}
