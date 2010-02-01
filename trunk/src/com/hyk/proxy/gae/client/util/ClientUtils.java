/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ClientUtils.java 
 *
 * @author Administrator [ 2010-2-1 | ÏÂÎç10:11:39 ]
 *
 */
package com.hyk.proxy.gae.client.util;

import java.io.IOException;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.hyk.proxy.gae.common.HttpResponseExchange;

/**
 *
 */
public class ClientUtils
{
	private static final String ContentRangeValueHeader = "bytes";
	
	public static HttpResponse buildHttpServletResponse(HttpResponseExchange forwardResponse) throws IOException
	{

		if(null == forwardResponse)
		{
			return new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT);
		}
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(200));
		// response.setStatus(forwardResponse.getResponseCode());
		List<String[]> headers = forwardResponse.getHeaders();
		for(String[] header : headers)
		{
			try
			{
				response.setHeader(header[0], header[1]);
			}
			catch(Exception e)
			{
				//System.out.println("#####" + header[0] + ":" + header[1]);
			}
			
		}
		byte[] content = forwardResponse.getBody();
		if(null != content)
		{
			ChannelBuffer bufer = ChannelBuffers.copiedBuffer(content);
			response.setContent(bufer);
		}
		
		return response;
	}
	
	public static long[] parseContentRange(String value)
	{
		String left = value.substring(ContentRangeValueHeader.length()).trim();
		String[] split = left.split("/");
		String[] split2 = split[0].split("-");
		long[] ret = new long[3];
		ret[0] =  Long.parseLong(split2[0].trim());
		ret[1] =  Long.parseLong(split2[1].trim());
		ret[2] =  Long.parseLong(split[1].trim());
		return ret;
	}
}
