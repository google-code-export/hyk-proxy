/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: ProxyServlet.java 
 *
 * @author qiying.wang [ Jan 12, 2010 | 2:02:10 PM ]
 *
 */
package com.hyk.proxy;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.io.ByteArrayBuffer;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;
import com.hyk.serializer.StandardSerializer;
import com.sun.net.httpserver.HttpHandler;

/**
 *
 */
public class ProxyServlet extends HttpServlet
{
	static HttpClient client = new HttpClient();

	static
	{
		try
		{
			client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
			client.start();
			
		}
		catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	protected HTTPRequest buildForwardRequest(HttpServletRequest request) throws IOException
	{
		URL url = new URL(request.getRequestURL().toString());
		HTTPRequest gaeRequest = new HTTPRequest(url, HTTPMethod.valueOf(request.getMethod()));
		Enumeration<String> headers = request.getHeaderNames();
		while(headers.hasMoreElements())
		{
			String headerName = headers.nextElement();
			Enumeration<String> headerValues = request.getHeaders(headerName);
			while(null != headerValues && headerValues.hasMoreElements())
			{
				String headerValue = headerValues.nextElement();
				gaeRequest.addHeader(new HTTPHeader(headerName, headerValue));
			}
		}

		int bodyLen = request.getContentLength();
		if(bodyLen > 0)
		{
			byte[] payload = new byte[bodyLen];
			request.getInputStream().read(payload);
			gaeRequest.setPayload(payload);
		}
		return gaeRequest;
	}

	protected void buildHttpServletResponse(HttpServletResponse response, HTTPResponse forwardResponse) throws IOException
	{
		response.setStatus(forwardResponse.getResponseCode());
		List<HTTPHeader> headers = forwardResponse.getHeaders();
		for(HTTPHeader header : headers)
		{
			response.setHeader(header.getName(), header.getValue());
		}
		byte[] content = forwardResponse.getContent();
		URL url = forwardResponse.getFinalUrl();
		if(null != url)
		{
			// redirect
		}
		response.getOutputStream().write(content);
		// response.
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		//super.service(request, response);
		try
		{
			HTTPRequest forwardRequest = buildForwardRequest(request);
		
			
			Serializer serializer = new StandardSerializer();
			ContentExchange contentExchange = new ContentExchange();
			contentExchange.setURL("http://localhost:8888/hyk_proxy");
			org.eclipse.jetty.io.Buffer buffer = new ByteArrayBuffer(serializer.serialize(forwardRequest));
			contentExchange.setRequestContent(buffer);
			contentExchange.setMethod("GET");

			client.send(contentExchange);
			contentExchange.waitForDone();
			byte[] responseContent = contentExchange.getResponseContentBytes();
			//HTTPResponse res = new HTTPResponse();
			if(contentExchange.getResponseStatus() != 200)
			{
				response.setStatus(contentExchange.getStatus());
				return;
			}
			HTTPResponse forwardResponse = serializer.deserialize(HTTPResponse.class, responseContent);
			buildHttpServletResponse(response, forwardResponse);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

	}

}
