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
import java.io.InputStream;
import java.io.NotSerializableException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.ContentExchange;
//import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.jivesoftware.smack.XMPPException;


import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;
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
	//static HttpClient	client	= new DefaultHttpClient();
	static Serializer serializer;
	static Compressor 	compressor = new SevenZipCompressor();
	static
	{
		serializer = new HykSerializer();
		
		try
		{
			//serializer.registerDefaultConstructor(HTTPResponse.class, 200);
			//serializer.registerDefaultConstructor(URL.class, "http://www.google.com");
			//client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
			//client.setProxy(new Address("127.0.0.1", 9666));
			//client.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	protected HttpRequestExchange buildForwardRequest(HttpServletRequest request) throws IOException
	{
		HttpRequestExchange gaeRequest = new HttpRequestExchange();
		StringBuffer urlbuffer = new StringBuffer();
		urlbuffer.append(request.getRequestURL().toString());
		urlbuffer.append("?").append(request.getQueryString());
		gaeRequest.setURL(urlbuffer.toString());
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
	}
	
	protected byte[] talkByHttp(byte[] request) throws TalkException, IOException, InterruptedException
	{
		HttpClient	client	= new DefaultHttpClient();
		HttpPost post = new HttpPost("http://hykserver.appspot.com/fetchproxy");
		HttpEntity e = new ByteArrayEntity(request);
		//System.out.println("####" + e.getContentType());
		post.setEntity(e);
		HttpResponse response = client.execute(post);
		
		HttpEntity resEntity = response.getEntity();
		InputStream entityIs = resEntity.getContent();
		byte[] resContent = new byte[0];
		if(resEntity.getContentLength() > 0)
		{
			resContent = new byte[(int) resEntity.getContentLength()];
			int offset = 0;
			int length = resContent.length;
			while(length != 0)
			{
				offset = entityIs.read(resContent,offset,length); 
				length -= offset;
			}
		}
		if(response.getStatusLine().getStatusCode() != 200)
		{
			throw new TalkException(response.getStatusLine().getStatusCode(), new String(resContent));
		}
		return resContent;
	}
	
	protected byte[] talkByXmpp(byte[] request) throws XMPPException, InterruptedException, Base64DecoderException, TalkException
	{
		return new XmppTalk().talk(request);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// super.service(request, response);
		try
		{
			HttpRequestExchange forwardRequest = buildForwardRequest(request);
			byte[] data = serializer.serialize(forwardRequest);
			data = compressor.compress(data);
			
			byte[] responseContent;
			try {
				//responseContent = talkByHttp(data);
				responseContent = talkByXmpp(data);
				//System.out.println("####Res len:" + responseContent.length);
			} catch (TalkException e) {
				response.sendError(e.getResCode(), e.getResCause());
				return;
			}
			
			//System.out.println("###Response size: " + responseContent.length);
			responseContent = compressor.decompress(responseContent);
			HttpResponseExchange forwardResponse = serializer.deserialize(HttpResponseExchange.class, responseContent);
			//forwardResponse.printMessage();
			if(null != forwardResponse.getRedirectURL())
			{
				//response.encodeRedirectURL(forwardResponse.getRedirectURL());
				//System.out.println("###Redirect " + response.encodeRedirectURL(forwardResponse.getRedirectURL()));
				response.sendRedirect(response.encodeRedirectURL(forwardResponse.getRedirectURL()));
			}
			//else
			{
				//response.
				buildHttpServletResponse(response, forwardResponse);
			}
			
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) throws NotSerializableException, IOException, InterruptedException
	{
		//HttpRequestExchange forwardRequest = buildForwardRequest(request);

		ContentExchange contentExchange = new ContentExchange();
		contentExchange.setURL("http://wangqiying.appspot.com/");
		byte[] data = "0012345".getBytes();
		org.eclipse.jetty.io.Buffer buffer = new ByteArrayBuffer(data);
		//contentExchange.setRequestContent(buffer);
		//
		contentExchange.setMethod("POST");
		contentExchange.setRequestContent(buffer);
		//client.send(contentExchange);
		contentExchange.waitForDone();
		//contentExchange.
		byte[] responseContent = contentExchange.getResponseContentBytes();
		// HTTPResponse res = new HTTPResponse();
		if(contentExchange.getResponseStatus() != 200)
		{
			System.out.println("#####Error Code" + contentExchange.getResponseStatus());
			//response.sendError(contentExchange.getResponseStatus(), contentExchange.getResponseContent());
			//response.setStatus(contentExchange.getStatus());
			System.out.println("#####Error Content:\n" + contentExchange.getResponseContent());
			return;
		}
		else
		{
			System.out.println("#####Content:\n" + contentExchange.getResponseContent());
		}
	}

}
