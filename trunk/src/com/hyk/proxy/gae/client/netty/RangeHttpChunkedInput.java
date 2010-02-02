/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RangeChunkedInput.java 
 *
 * @author qiying.wang [ Feb 2, 2010 | 10:53:27 AM ]
 *
 */
package com.hyk.proxy.gae.client.netty;

import org.jboss.netty.handler.stream.ChunkedInput;

import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.service.FetchService;

/**
 *
 */
public class RangeHttpChunkedInput implements ChunkedInput
{
	private FetchService			fetchService;
	private HttpRequestExchange		forwardRequest;
	
	private int totalLen;
	private int cursor;
	private int step;
	
	@Override
	public void close() throws Exception
	{
		//nothing now
	}

	@Override
	public boolean hasNextChunk() throws Exception
	{
		return cursor < totalLen;
	}

	@Override
	public Object nextChunk() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

}
