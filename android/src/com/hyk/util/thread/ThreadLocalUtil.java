/**
 * This file is part of the hyk-util project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ThreadLocalUtil.java 
 *
 * @author Administrator [ 2010-1-30 | 01:31:15 PM ]
 *
 */
package com.hyk.util.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class ThreadLocalUtil<T>
{
	private static Map<Class, ThreadLocalUtil> instatnceTable = new ConcurrentHashMap<Class, ThreadLocalUtil>();
	private ThreadLocal<T>	objTable	= new ThreadLocal<T>();

	private ThreadLocalUtil(){}
	
	public T getThreadLocalObject()
	{
		return objTable.get();
	}

	public void setThreadLocalObject(T obj)
	{
		objTable.set(obj);
	}
	
	public static <A> ThreadLocalUtil<A> getThreadLocalUtil(Class<A> type)
	{
		if(!instatnceTable.containsKey(type))
		{
			instatnceTable.put(type, new ThreadLocalUtil());
		}
		return instatnceTable.get(type);
	}
}
