/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: HttpMessageExhange.java 
 *
 * @author qiying.wang [ Jan 14, 2010 | 4:02:29 PM ]
 *
 */
package com.hyk.proxy.gae.common;

//import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import com.hyk.serializer.Externalizable;
import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;
import com.hyk.serializer.io.HykObjectInput;

/**
 *
 */
public abstract class HttpMessageExhange implements Externalizable
{
	protected ArrayList<String[]> headers = new ArrayList<String[]>();
	protected byte[] body;
	
	//protected transient Compressor 	compressor = new GZipCompressor();
	
	public void addHeader(String name, String value)
	{
		headers.add(new String[]{name, value});
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
		for (String[] header : headers) {
			if(header[0].equals("Content-Length"))
			{
				return Integer.parseInt(header[1]);
			}
		}
		return 0;
	}
	
	public List<String[]> getHeaders()
	{
		return headers;
	}
	
	protected abstract void print();
	public void printMessage()
	{
		System.out.println("#########");
		print();
		for (String[] header : headers) {
			System.out.println(header[0] + ": " + header[1]);
		}
		System.out.println("#########");
		System.out.println();
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
//			int len = in.readInt();
//			body = new byte[len];
//			//System.out.println("####Expected read " + len);
//			in.read(body);
			//body = hin.readObject(byte[].class);
		}
	}

	public void writeExternal(SerializerOutput out) throws IOException
	{	
		out.writeInt(headers.size());
		for(String[] header:headers)
		{
			out.writeObject(header);
		}
		out.writeBoolean(null != body&& body.length > 0);
		if(null != body && body.length > 0)
		{
//			byte[] compress = compressor.compress(body);
//			out.writeObject(compress);
			//out.writeObject(body);
			//out.writeInt(body.length);
			out.writeObject(body);
		}
	}
}
