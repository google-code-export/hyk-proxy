/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RpcUtil.java 
 *
 * @author qiying.wang [ Mar 2, 2010 | 2:57:43 PM ]
 *
 */
package com.hyk.rpc.core.util;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import com.hyk.rpc.core.RpcException;
import com.hyk.rpc.core.annotation.Async;
import com.hyk.rpc.core.remote.RemoteObjectAyncProxy;
import com.hyk.rpc.core.remote.RemoteObjectProxy;

/**
 *
 */
public class RpcUtil
{
	public static <T> T asyncWrapper(Object obj, Class<T> clazz) throws RpcException
	{
		if(!clazz.isAnnotationPresent(Async.class))
		{
			throw new RpcException(clazz + "is not annotated by " + Async.class);
		}
		Async async = clazz.getAnnotation(Async.class);
		Class[] types = async.value();
		Class[] remoteTypes = RemoteUtil.getRemoteInterfaces(obj.getClass());
		if(!Arrays.equals(types, remoteTypes))
		{
			throw new RpcException(clazz + "is not compatible with paramter " + obj.getClass());
		}
		RemoteObjectProxy handler = (RemoteObjectProxy)Proxy.getInvocationHandler(obj);
		RemoteObjectProxy clone = new RemoteObjectAyncProxy(handler);
		return (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, clone);
	}
}
