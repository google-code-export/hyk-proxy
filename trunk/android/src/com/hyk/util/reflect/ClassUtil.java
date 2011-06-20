/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: ClassUtil.java 
 *
 * @author qiying.wang [ Jan 14, 2010 | 2:36:30 PM ]
 *
 */
package com.hyk.util.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class ClassUtil
{
	private static Map<Class, Method[]>		methodCacheTable				= new ConcurrentHashMap<Class, Method[]>();
	
	/** A map from primitive types to their corresponding wrapper types. */
	public static final Map<Class<?>, Class<?>>	PRIMITIVE_TO_WRAPPER_TYPE;

	/** A map from wrapper types to their corresponding primitive types. */
	public static final Map<Class<?>, Class<?>>	WRAPPER_TO_PRIMITIVE_TYPE;

	static
	{
		Map<Class<?>, Class<?>> primToWrap = new HashMap<Class<?>, Class<?>>(16);
		Map<Class<?>, Class<?>> wrapToPrim = new HashMap<Class<?>, Class<?>>(16);

		add(primToWrap, wrapToPrim, boolean.class, Boolean.class);
		add(primToWrap, wrapToPrim, byte.class, Byte.class);
		add(primToWrap, wrapToPrim, char.class, Character.class);
		add(primToWrap, wrapToPrim, double.class, Double.class);
		add(primToWrap, wrapToPrim, float.class, Float.class);
		add(primToWrap, wrapToPrim, int.class, Integer.class);
		add(primToWrap, wrapToPrim, long.class, Long.class);
		add(primToWrap, wrapToPrim, short.class, Short.class);
		add(primToWrap, wrapToPrim, void.class, Void.class);

		PRIMITIVE_TO_WRAPPER_TYPE = Collections.unmodifiableMap(primToWrap);
		WRAPPER_TO_PRIMITIVE_TYPE = Collections.unmodifiableMap(wrapToPrim);
	}

	private static void add(Map<Class<?>, Class<?>> forward, Map<Class<?>, Class<?>> backward, Class<?> key, Class<?> value)
	{
		forward.put(key, value);
		backward.put(value, key);
	}

	public static boolean equals(Class a, Class b)
	{
		boolean ret = a.equals(b);
		if(!ret)
		{
			if(a.isPrimitive() || b.isPrimitive())
			{
				Class wrapper = PRIMITIVE_TO_WRAPPER_TYPE.get(a.isPrimitive() ? a : b);
				return wrapper.equals(a.isPrimitive() ? b : a);
			}
		}
		return ret;
	}
	
//	public static int getMethodID(Method method, Class[] interfaceClasses)
//	{
//		
//		List<Method> allMethods = new LinkedList<Method>();
//		for(Class clazz:interfaceClasses)
//		{
//			allMethods.addAll(Arrays.asList(clazz.getMethods()));
//		}
//		Collections.sort(allMethods);
//		String name = method.getName();
//		Class[] paraTypes = method.getParameterTypes();
//		return -1;
//	}
	
	public static Method[] getMethods(Class clazz)
	{
		Method[] ms = methodCacheTable.get(clazz);
		if(null == ms)
		{
			ms = clazz.getMethods();
			methodCacheTable.put(clazz, ms);
		}
		return ms;
	}
	
	public static Method getMethod(Class clazz, String methodName, Object... paras) throws NoSuchMethodException
	{
		Method ret = null;
		if(null == paras)
		{
			paras = new Object[0];
		}
		Method[] allMethods = getMethods(clazz);
		for(Method method : allMethods)
		{
			if(!method.getName().equals(methodName))
			{
				continue;
			}
			Class[] types = method.getParameterTypes();
			if(types.length != paras.length)
				continue;
			boolean matched = true;
			for(int i = 0; i < types.length; i++)
			{
				if(null == paras[i])
				{
					continue;
				}
				Class declType = types[i];
				Class realType = paras[i].getClass();
				if(declType.isAssignableFrom(realType))
				{
					continue;
				}
				Class convertType = PRIMITIVE_TO_WRAPPER_TYPE.get(declType);
				if(null == convertType)
				{
					convertType = WRAPPER_TO_PRIMITIVE_TYPE.get(declType);
				}
				if(null != convertType)
				{
					if(convertType.equals(realType))
					{
						continue;
					}
				}
				matched = false;
				break;
			}
			if(matched)
			{
				ret = method;
			}
		}
		if(null == ret)
			throw new NoSuchMethodException(methodName + " for " + clazz.getName());
		return ret;
	}
}
