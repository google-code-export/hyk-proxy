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

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.stream.ChunkedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.client.config.Config;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.proxy.gae.common.http.RangeHeaderValue;
import com.hyk.proxy.gae.common.service.FetchService;

/**
 *
 */
public class RangeHttpProxyChunkedInput implements ChunkedInput
{
	protected Logger				logger			= LoggerFactory.getLogger(getClass());
	private FetchService		fetchService;
	private HttpRequestExchange	forwardRequest;

	private long					totalLen;
	private long					step;
	private long					cursor;

	public RangeHttpProxyChunkedInput(FetchService fetchService, HttpRequestExchange forwardRequest, long total) throws IOException
	{
		this.fetchService = fetchService;
		this.forwardRequest = forwardRequest;
		totalLen = total;
		step = Config.getInstance().getFetchLimitSize();
		cursor = step;
	}
	
	@Override
	public void close() throws Exception
	{
		// nothing now
	}

	@Override
	public boolean hasNextChunk() throws Exception
	{
		return cursor < totalLen;
	}

	@Override
	public Object nextChunk() throws Exception
	{
		RangeHeaderValue value = new RangeHeaderValue(cursor, cursor += (step - 1));
		cursor += 1;
		forwardRequest.setHeader(HttpHeaders.Names.RANGE, value);
		if(logger.isDebugEnabled())
		{
			logger.debug("Send proxy request");
			logger.debug(forwardRequest.toPrintableString());
		}
		HttpResponseExchange response = fetchService.fetch(forwardRequest);
		if(logger.isDebugEnabled())
		{
			logger.debug("Recv proxy response");
			logger.debug(response.toPrintableString());
		}
		return ChannelBuffers.wrappedBuffer(response.getBody());
	}
}
