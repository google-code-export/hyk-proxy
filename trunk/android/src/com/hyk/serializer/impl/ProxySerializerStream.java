/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: ProxySerializer.java 
 *
 * @author qiying.wang [ Jan 21, 2010 | 1:50:36 PM ]
 *
 */
package com.hyk.serializer.impl;

import java.io.IOException;
import java.io.NotSerializableException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.serializer.util.ContextUtil;

/**
 *
 */
public class ProxySerializerStream<T> extends SerailizerStream<T>
{

	@Override
	protected T unmarshal(Class<T> type, ChannelDataBuffer data)
	        throws NotSerializableException, IOException,
	        InstantiationException
	{
		try
		{
			String[] interfaceNames = readObject(data, String[].class);
			Class[] interfaces = new Class[interfaceNames.length];
			ClassLoader loader = ContextUtil.getDeserializeClassLoader();
			for (int i = 0; i < interfaceNames.length; i++)
			{
				interfaces[i] = Class.forName(interfaceNames[i], true, loader);
			}
			Class proxyHandlerClass = Class.forName(readString(data), true,
			        loader);
			InvocationHandler handler = (InvocationHandler) readObject(data,
			        proxyHandlerClass);
			T ret = (T) Proxy.newProxyInstance(loader, interfaces, handler);
			ContextUtil.addDeserializeThreadLocalObject(ret);
			return ret;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	@Override
	protected ChannelDataBuffer marshal(T value, ChannelDataBuffer data)
	        throws NotSerializableException, IOException
	{
		Class[] interfaces = value.getClass().getInterfaces();
		String[] interfaceNames = new String[interfaces.length];
		for (int i = 0; i < interfaceNames.length; i++)
		{
			interfaceNames[i] = interfaces[i].getName();
		}
		writeObject(data, interfaceNames);
		InvocationHandler handler = Proxy.getInvocationHandler(value);
		writeString(data, handler.getClass().getName());
		writeObject(data, handler);
		return data;
	}

}
