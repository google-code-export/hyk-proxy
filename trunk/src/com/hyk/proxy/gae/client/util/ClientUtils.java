/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ClientUtils.java 
 *
 * @author Administrator [ 2010-2-1 | pm10:11:39 ]
 *
 */
package com.hyk.proxy.gae.client.util;

import java.io.IOException;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.proxy.gae.common.http.SetCookieHeaderValue;

/**
 *
 */
public class ClientUtils
{
	private static final String	ContentRangeValueHeader	= "bytes";

	public static HttpResponse buildHttpServletResponse(HttpResponseExchange forwardResponse) throws IOException
	{

		if(null == forwardResponse)
		{
			return new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT);
		}
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(forwardResponse.getResponseCode()));

		List<String[]> headers = forwardResponse.getHeaders();
		for(String[] header : headers)
		{
			if(header[0].equalsIgnoreCase(HttpHeaders.Names.SET_COOKIE) || header[0].equalsIgnoreCase(HttpHeaders.Names.SET_COOKIE2))
			{
				List<SetCookieHeaderValue> cookies = SetCookieHeaderValue.parse(header[1]);
				for(SetCookieHeaderValue cookie : cookies)
				{
					response.addHeader(header[0], cookie.toString());
					
				}
			}
			else
			{
				response.addHeader(header[0], header[1]);
			}

		}
		byte[] content = forwardResponse.getBody();
		if(null != content)
		{
			ChannelBuffer bufer = ChannelBuffers.wrappedBuffer(content);
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
		ret[0] = Long.parseLong(split2[0].trim());
		ret[1] = Long.parseLong(split2[1].trim());
		ret[2] = Long.parseLong(split[1].trim());
		return ret;
	}

	public static boolean isCompleteResponse(HttpResponseExchange response)
	{
		String contentRange = response.getHeaderValue(HttpHeaders.Names.CONTENT_RANGE);
		if(null == contentRange)
		{
			return true;
		}
		long[] lens = parseContentRange(contentRange);
		if(lens[1] >= (lens[2] - 1))
		{
			return true;
		}
		return false;
	}
}
