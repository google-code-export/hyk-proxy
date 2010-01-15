/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: GaeProxyClientServlet.java 
 *
 * @author qiying.wang [ Jan 14, 2010 | 3:24:29 PM ]
 *
 */
package com.hyk.proxy.gae.client;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.ByteArrayBuffer;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.hyk.compress.Compressor;
import com.hyk.compress.gz.GZipCompressor;
import com.hyk.compress.sevenzip.SevenZipCompressor;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;
import com.hyk.serializer.StandardSerializer;

/**
 *
 */
public class GaeProxyClientServlet extends HttpServlet
{
	static HttpClient	client	= new HttpClient();
	static Serializer serializer;
	static Compressor 	compressor = new SevenZipCompressor();
	static
	{
		serializer = new HykSerializer();
		
		try
		{
			//serializer.registerDefaultConstructor(HTTPResponse.class, 200);
			//serializer.registerDefaultConstructor(URL.class, "http://www.google.com");
			client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
			client.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	protected HttpRequestExchange buildForwardRequest(HttpServletRequest request) throws IOException
	{
		//URL url = new URL(request.getRequestURL().toString());
		HttpRequestExchange gaeRequest = new HttpRequestExchange();
		gaeRequest.setURL(request.getRequestURL().toString());
		gaeRequest.setMethod(request.getMethod());
		Enumeration<String> headers = request.getHeaderNames();
		while(headers.hasMoreElements())
		{
			String headerName = headers.nextElement();
			Enumeration<String> headerValues = request.getHeaders(headerName);
			while(null != headerValues && headerValues.hasMoreElements())
			{
				String headerValue = headerValues.nextElement();
				gaeRequest.addHeader(headerName, headerValue);
			}
		}

		int bodyLen = request.getContentLength();
		if(bodyLen > 0)
		{
			byte[] payload = new byte[bodyLen];
			request.getInputStream().read(payload);
			gaeRequest.setBody(payload);
		}
		return gaeRequest;
	}

	protected void buildHttpServletResponse(HttpServletResponse response, HttpResponseExchange forwardResponse) throws IOException
	{
		response.setStatus(forwardResponse.getResponseCode());
		List<String[]> headers = forwardResponse.getHeaders();
		for(String[] header : headers)
		{
			response.setHeader(header[0], header[1]);
		}
		byte[] content = forwardResponse.getBody();
		if(null != content)
		{
			response.getOutputStream().write(content);
		}
		
		// response.
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// super.service(request, response);
		try
		{
			HttpRequestExchange forwardRequest = buildForwardRequest(request);

			ContentExchange contentExchange = new ContentExchange();
			contentExchange.setURL("http://localhost:8888/fetchproxy");
			byte[] data = serializer.serialize(forwardRequest);
			data = compressor.compress(data);
			System.out.println("####Encode req" + data.length);
			org.eclipse.jetty.io.Buffer buffer = new ByteArrayBuffer(data);
			contentExchange.setRequestContent(buffer);
			contentExchange.setMethod("GET");
		
			client.send(contentExchange);
			contentExchange.waitForDone();
			byte[] responseContent = contentExchange.getResponseContentBytes();
			// HTTPResponse res = new HTTPResponse();
			if(contentExchange.getResponseStatus() != 200)
			{
				response.setStatus(contentExchange.getStatus());
				return;
			}
			responseContent = compressor.decompress(responseContent);
			HttpResponseExchange forwardResponse = serializer.deserialize(HttpResponseExchange.class, responseContent);
			buildHttpServletResponse(response, forwardResponse);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

	}

}
