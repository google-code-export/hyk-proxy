/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ClientUtils.java 
 *
 * @author yinqiwen [ 2010-2-1 | 10:11:39 PM ]
 *
 */
package com.hyk.proxy.gae.client.util;

import java.io.IOException;
import java.security.KeyStore;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.client.httpserver.HttpServer;
import com.hyk.proxy.gae.common.http.header.SetCookieHeaderValue;
import com.hyk.proxy.gae.common.http.message.HttpResponseExchange;
import com.hyk.util.buffer.ByteArray;

/**
 *
 */
public class ClientUtils
{
	protected static Logger				logger			= LoggerFactory.getLogger(ClientUtils.class);
	private static final String	ContentRangeValueHeader	= "bytes";

	public static SSLContext initSSLContext() throws Exception
	{
		String password = "hyk-proxy";
		SSLContext sslContext = SSLContext.getInstance("TLS");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(HttpServer.class.getResourceAsStream("/hyk-proxy.cert"), password.toCharArray());
		kmf.init(ks, password.toCharArray());
		KeyManager[] km = kmf.getKeyManagers();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		TrustManager[] tm = tmf.getTrustManagers();
		sslContext.init(km, tm, null);
		return sslContext;
	}
	
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
		ByteArray content = forwardResponse.getBody();
		if(null != content)
		{
			ChannelBuffer bufer = ChannelBuffers.wrappedBuffer(content.buffer());
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
