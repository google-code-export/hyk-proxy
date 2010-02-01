/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: FetchTask.java 
 *
 * @author Administrator [ 2010-2-1 | ÏÂÎç09:57:56 ]
 *
 */
package com.hyk.proxy.gae.client.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.client.util.ClientUtils;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.proxy.gae.common.service.FetchService;

/**
 *
 */
public class HttpFetchTask implements Runnable
{
	public HttpFetchTask(Channel channel, FetchService setchService, HttpRequestExchange forwardRequest)
	{
		super();
		this.channel = channel;
		this.setchService = setchService;
		this.forwardRequest = forwardRequest;
	}

	protected Logger				logger	= LoggerFactory.getLogger(getClass());

	private Channel					channel;
	private FetchService			setchService;
	private HttpRequestExchange		forwardRequest;
	private HttpResponseExchange	lastForwardResponse;
	private int						rangeStep = 64000;

	protected boolean fetch()
	{
		
		long start = 0;
		long end = rangeStep;
		if(null != lastForwardResponse)
		{
			String value = lastForwardResponse.getHeaderValue(HttpHeaders.Names.CONTENT_RANGE);
			if(null == value)
			{
				return true;
			}
			long[] lens = ClientUtils.parseContentRange(value);
			if(lens[1] >= (lens[2] - 1))
			{
				return true;
			}
			start = lens[1] + 1;
			end = start + rangeStep;
			if(end >= lens[2])
			{
				end = lens[2] - 1;
			}
		}
		StringBuilder headerValue = new StringBuilder();
		headerValue.append("bytes=").append(start).append("-").append(end);
		forwardRequest.setHeader(HttpHeaders.Names.RANGE, headerValue.toString());
		lastForwardResponse = setchService.fetch(forwardRequest);
		lastForwardResponse.printMessage();
		return false;
	}

	@Override
	public void run()
	{
		fetch();
		try
		{
			if(channel.isConnected())
			{
				HttpResponse res = ClientUtils.buildHttpServletResponse(lastForwardResponse);
				String value = lastForwardResponse.getHeaderValue(HttpHeaders.Names.CONTENT_RANGE);
				if(null != value)
				{
					System.out.println("####" + String.valueOf(ClientUtils.parseContentRange(value)[2]));
					res.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(ClientUtils.parseContentRange(value)[2]));
				}
				long[] lens = ClientUtils.parseContentRange(value);
				res.removeHeader(HttpHeaders.Names.CONTENT_RANGE);
				res.removeHeader(HttpHeaders.Names.ACCEPT_RANGES);
				
				
				//res.
				ChannelFuture future = channel.write(res);
				future.addListener(ChannelFutureListener.CLOSE);
			}
			while(!fetch())
			{
				ChannelBuffer content = ChannelBuffers.copiedBuffer(lastForwardResponse.getBody());
				HttpChunk chunk = new DefaultHttpChunk(content);
				if(channel.isConnected())
				{
					HttpResponse res = ClientUtils.buildHttpServletResponse(lastForwardResponse);
					ChannelFuture future = channel.write(res);
					future.addListener(ChannelFutureListener.CLOSE);
				}
			}
			return;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}

}
