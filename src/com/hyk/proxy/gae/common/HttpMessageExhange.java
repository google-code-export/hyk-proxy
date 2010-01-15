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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hyk.serializer.io.HykObjectInput;

/**
 *
 */
public class HttpMessageExhange implements Externalizable
{
	protected ArrayList<String[]> headers = new ArrayList<String[]>();
	protected byte[] body;
	
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
	
	public List<String[]> getHeaders()
	{
		return headers;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int size = in.readInt();
		if(in instanceof HykObjectInput)
		{
			HykObjectInput hin = (HykObjectInput)in;	
			int i = 0;
			while(i < size)
			{
				String[] header = hin.readObject(String[].class);
				headers.add(header);
				i++;
			}
			body = hin.readObject(byte[].class);
		}

	}

	public void writeExternal(ObjectOutput out) throws IOException
	{	
		out.writeInt(headers.size());
		for(String[] header:headers)
		{
			out.writeObject(header);
		}
		if(null != body && body.length > 0)
		{
			out.writeObject(body);
		}
	}
}
