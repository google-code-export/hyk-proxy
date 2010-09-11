/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BandwidthStatisticsService.java 
 *
 * @author yinqiwen [ 2010-4-5 | 07:06:15 PM]
 *
 */
package com.hyk.proxy.common.rpc.service;


import java.util.List;

import com.hyk.proxy.common.gae.stat.BandwidthStatisticsResult;
import com.hyk.rpc.core.annotation.Remote;



/**
 *
 */
@Remote
public interface BandwidthStatisticsService
{	
	public BandwidthStatisticsResult getStatResult(String host);
	public List<BandwidthStatisticsResult> getStatResults(int limit);
	
	//public void statBandwidth(String host, long incoming, long outgoing);
	public void clear();
	public void enable(boolean flag);
	public boolean isEnable();
}
