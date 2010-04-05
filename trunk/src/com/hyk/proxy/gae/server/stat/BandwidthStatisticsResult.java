/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BandwidthStatisticsResult.java 
 *
 * @author yinqiwen [ 2010-4-5 | ÏÂÎç07:01:34 ]
 *
 */
package com.hyk.proxy.gae.server.stat;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
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

	@PrimaryKey
	private String targetSiteHost;
	
	@Persistent
	private long outgoing;
	
	@Persistent
	private long Incoming;
	
}
