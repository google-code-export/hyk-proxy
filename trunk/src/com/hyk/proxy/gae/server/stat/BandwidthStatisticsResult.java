/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BandwidthStatisticsResult.java 
 *
 * @author yinqiwen [ 2010-4-5 | 07:01:34 PM ]
 *
 */
package com.hyk.proxy.gae.server.stat;

import javax.persistence.Id;

/**
 *
 */
public class BandwidthStatisticsResult
{
	public String getTargetSiteHost()
	{
		return targetSiteHost;
	}

	public void setTargetSiteHost(String targetSiteHost)
	{
		this.targetSiteHost = targetSiteHost;
	}

	public long getOutgoing()
	{
		return outgoing;
	}

	public void setOutgoing(long outgoing)
	{
		this.outgoing = outgoing;
	}

	public long getIncoming()
	{
		return Incoming;
	}

	public void setIncoming(long incoming)
	{
		Incoming = incoming;
	}

	@Id
	private String targetSiteHost;
	
	private long outgoing;
	
	private long Incoming;
	
}
