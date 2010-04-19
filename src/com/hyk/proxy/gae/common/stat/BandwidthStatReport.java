/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BandwidthStatReport.java 
 *
 * @author qiying.wang [ Apr 19, 2010 | 2:04:36 PM ]
 *
 */
package com.hyk.proxy.gae.common.stat;

import java.io.Serializable;

/**
 *
 */
public class BandwidthStatReport implements Serializable
{
	String host;
	long incoming;
	long outgoing;
	
	public BandwidthStatReport(String host, long incoming, long outgoing)
	{
		this.host = host;
		this.incoming = incoming;
		this.outgoing = outgoing;
	}
	
	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public long getIncoming()
	{
		return incoming;
	}

	public void setIncoming(long incoming)
	{
		this.incoming = incoming;
	}

	public long getOutgoing()
	{
		return outgoing;
	}

	public void setOutgoing(long outgoing)
	{
		this.outgoing = outgoing;
	}
}
