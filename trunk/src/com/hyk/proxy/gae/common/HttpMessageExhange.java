/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: HttpMessageExhange.java 
 *
 * @author yinqiwen [ Jan 14, 2010 | 4:02:29 PM ]
 *
 */
package com.hyk.proxy.gae.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hyk.proxy.gae.common.http.HttpHeaderValue;
import com.hyk.serializer.Externalizable;
import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 *
 */
public abstract class HttpMessageExhange implements Externalizable
{
	protected List<String[]>	headers	= new ArrayList<String[]>();
	protected byte[]				body	= new byte[0];

	
	@Override
	public int hashCode()
	{
		int hash = 0;
		for(String[] header : headers)
		{
			if( header[0].equalsIgnoreCase("range"))
			{
				hash += header[0].hashCode();
				hash += header[1].hashCode();
			}
		}
		return hash;
	}
	
	protected boolean compareHeaders(HttpMessageExhange other)
	{
		if(containsHeader("range") && other.containsHeader("range"))
		{
			String range1 = getHeaderValue("range");
			String range2 = getHeaderValue("range");
			if(!range1.equalsIgnoreCase(range2))
			{
				return false;
			}
		}
		else if(containsHeader("range") || other.containsHeader("range"))
		{
			return false;
		}
		return true;
//		if(headers.size() != other.headers.size())
//		{
//			return false;
//		}
//		for(int i = 0; i< headers.size(); i++)
//		{
//			String[] header1 = headers.get(i);
//			String[] header2 = other.headers.get(i);
//			if( header1[0].equalsIgnoreCase("range"))
//			{
//				if(!(header1[0].equalsIgnoreCase(header2[0]) && header1[1].equalsIgnoreCase(header2[1])))
//				{
//					return false;
//				}
//			}
//		}
//		return true;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(null == obj)
		{
			return false;
		}
		if(obj instanceof HttpMessageExhange)
		{
			HttpMessageExhange other = (HttpMessageExhange)obj;
			return compareHeaders(other) &&  Arrays.equals(body, other.body);
		}
		return false;
	}
	
	public void addHeader(String name, String value)
	{
		headers.add(new String[] {name, value});
	}
	
	public void addHeader(String name, HttpHeaderValue value)
	{
		headers.add(new String[] {name, value.toString()});
	}
	
	public void setHeader(String name, HttpHeaderValue value)
	{
		setHeader(name, value.toString());
	}
	
	public void setHeaders(List<String[]> headers)
	{
		this.headers = headers;
	}
	
	public void setHeader(String name, String value)
	{
		for(String[] header : headers)
		{
			if(header[0].equalsIgnoreCase(name))
			{
				header[1] = value;
				return;
			}
		}
		addHeader(name, value);
	}

	public void setBody(byte[] data)
	{
		this.body = data;
	}

	public byte[] getBody()
	{
		return body;
	}

	public int getContentLength()
	{
		for(String[] header : headers)
		{
			if(header[0].equalsIgnoreCase("Content-Length"))
			{
				return Integer.parseInt(header[1]);
			}
		}
		return 0;
	}
	
	public boolean containsHeader(String name)
	{
		for(String[] header : headers)
		{
			if(header[0].equals(name))
			{
				return true;
			}
		}
		return false;
	}
	
	public String getHeaderValue(String name)
	{
		for(String[] header : headers)
		{
			if(header[0].equalsIgnoreCase(name))
			{
				return header[1];
			}
		}
		return null;
	}
	
	public String removeHeader(String name)
	{
		for(int i = 0; i< headers.size(); i++)
		{
			String[] header = headers.get(i);
			if(header[0].equalsIgnoreCase(name))
			{
				headers.remove(i);
				return header[1];
			}
		}
		return null;
	}

	public List<String[]> getHeaders()
	{
		return headers;
	}
	
	public List<String[]> getCloneHeaders()
	{
		ArrayList<String[]> newHeaders = new ArrayList<String[]>();
		for(String[] header:headers)
		{
			newHeaders.add(new String[]{header[0], header[1]});
		}
		return newHeaders;
	}

	protected abstract void print(StringBuffer buffer);

	public String toPrintableString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("\r\n============================================\r\n");
		print(buffer);
		for(String[] header : headers)
		{
			buffer.append(header[0]).append(":").append(header[1]).append("\r\n");
		}
		buffer.append("============================================\r\n");
		return buffer.toString();
	}

	public void readExternal(SerializerInput in) throws IOException
	{
		int size = in.readInt();
		int i = 0;
		while(i < size)
		{
			String[] header = in.readObject(String[].class);
			headers.add(header);
			i++;
		}
		boolean b = in.readBoolean();
		if(b)
		{
			body = in.readObject(byte[].class);
		}
	}

	public void writeExternal(SerializerOutput out) throws IOException
	{
		out.writeInt(headers.size());
		for(String[] header : headers)
		{
			out.writeObject(header);
		}
		out.writeBoolean(null != body && body.length > 0);
		if(null != body && body.length > 0)
		{
			out.writeObject(body);
		}
	}
}
