/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RangeHeaderValue.java 
 *
 * @author qiying.wang [ Feb 2, 2010 | 2:48:32 PM ]
 *
 */
package com.hyk.proxy.gae.common.http;

/**
 *
 */
public class RangeHeaderValue  implements HttpHeaderValue
{
	private static final String BYTES_UNIT = "bytes";

	private long firstBytePos;

	private long lastBytePos;
	
	public RangeHeaderValue(long firstBytePos, long lastBytePos)
	{
		this.firstBytePos = firstBytePos;
		this.lastBytePos = lastBytePos;
	}
	
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(BYTES_UNIT).append("=").append(firstBytePos).append("-").append(lastBytePos);
		return buffer.toString();
	}
	
	
}
