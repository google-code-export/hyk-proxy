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

import java.io.Serializable;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 *
 */
@Cached
public class BandwidthStatisticsResult implements Serializable
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
		return incoming;
	}

	public void setIncoming(long incoming)
	{
		this.incoming = incoming;
	}

	public BandwidthStatisticsResult()
	{
		
	}
	
	public BandwidthStatisticsResult(String targetSiteHost, long outgoing, long incoming)
	{
		this.targetSiteHost = targetSiteHost;
		this.outgoing = outgoing;
		this.incoming = incoming;
	}
	
	@Id
	private String targetSiteHost;

	private long outgoing;
	
	private long incoming;
	
}
