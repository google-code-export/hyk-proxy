/**
 * 
 */
package com.hyk.serializer.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.serializer.io.Type;

/**
 * @author qiying.wang
 * 
 */
public class ReflectionCache
{
	protected static Logger			logger					= LoggerFactory.getLogger(ReflectionCache.class);
	private static Map<Class, List<Type>>	typeListCacheTable				= new ConcurrentHashMap<Class, List<Type>>();
	private static Map<Class, Field[]>		fieldCacheTable					= new ConcurrentHashMap<Class, Field[]>();
	private static Map<Class, Method[]>		methodCacheTable				= new ConcurrentHashMap<Class, Method[]>();
	private static Map<Class, Constructor>	defaultConstructorCacheTable	= new ConcurrentHashMap<Class, Constructor>();
	private static Map<Class, Type>			reservedClassTable				= new HashMap<Class, Type>();
	static
	{
		reservedClassTable.put(byte.class, Type.BYTE);
		reservedClassTable.put(Byte.class, Type.BYTE);
		reservedClassTable.put(char.class, Type.CHAR);
		reservedClassTable.put(Character.class, Type.CHAR);
		reservedClassTable.put(float.class, Type.FLOAT);
		reservedClassTable.put(Float.class, Type.FLOAT);
		reservedClassTable.put(double.class, Type.DOUBLE);
		reservedClassTable.put(Double.class, Type.DOUBLE);
		reservedClassTable.put(int.class, Type.INT);
		reservedClassTable.put(Integer.class, Type.INT);
		reservedClassTable.put(short.class, Type.SHORT);
		reservedClassTable.put(Short.class, Type.SHORT);
		reservedClassTable.put(boolean.class, Type.BOOL);
		reservedClassTable.put(Boolean.class, Type.BOOL);
		reservedClassTable.put(float.class, Type.FLOAT);
		reservedClassTable.put(Float.class, Type.FLOAT);
		reservedClassTable.put(long.class, Type.LONG);
		reservedClassTable.put(Long.class, Type.LONG);
		reservedClassTable.put(String.class, Type.STRING);
		reservedClassTable.put(StringBuffer.class, Type.STRING);
		reservedClassTable.put(StringBuilder.class, Type.STRING);
		reservedClassTable.put(byte[].class, Type.ARRAY);
		reservedClassTable.put(char[].class, Type.ARRAY);
		reservedClassTable.put(int[].class, Type.ARRAY);
		reservedClassTable.put(short[].class, Type.ARRAY);
		reservedClassTable.put(long[].class, Type.ARRAY);
		reservedClassTable.put(float[].class, Type.ARRAY);
		reservedClassTable.put(double[].class, Type.ARRAY);
		reservedClassTable.put(HashMap.class, Type.OTHER);
		reservedClassTable.put(ConcurrentHashMap.class, Type.OTHER);
		reservedClassTable.put(LinkedList.class, Type.OTHER);
		reservedClassTable.put(Hashtable.class, Type.OTHER);
		reservedClassTable.put(ArrayList.class, Type.OTHER);
		reservedClassTable.put(Vector.class, Type.OTHER);
		reservedClassTable.put(HashSet.class, Type.OTHER);
		reservedClassTable.put(TreeSet.class, Type.OTHER);
	}

	public static Constructor getDefaultConstructor(Class clazz)
	{
		if(defaultConstructorCacheTable.containsKey(clazz))
		{
			return defaultConstructorCacheTable.get(clazz);
		}
		Constructor cons = null;
		try
		{
			cons = clazz.getDeclaredConstructor(null);
			cons.setAccessible(true);
			defaultConstructorCacheTable.put(clazz, cons);
		}
		catch(Exception e)
		{
			// System.out.println("%%%%%" + clazz.getName());
		}

		return cons;
	}

	public static ArrayList<Field> getAllDeaclaredFields(Class clazz)
	{
		if(null == clazz || clazz.isPrimitive())
		{
			return null;
		}
		ArrayList<Field> ret = new ArrayList<Field>(32);
		while(!clazz.equals(Object.class))
		{
			Field[] fs = clazz.getDeclaredFields();
			for(Field field : fs)
			{
				if(Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
				{
					continue;
				}
				try
				{
					if(!Modifier.isPublic(field.getModifiers()))
					{
						field.setAccessible(true);
					}
				}
				catch(Exception e)
				{
					return null;
				}	
				ret.add(field);
			}
			clazz = clazz.getSuperclass();
		}

		return ret;

	}

	public static Field[] getSerializableFields(Class clazz)
	{
		Field[] fs = fieldCacheTable.get(clazz);
		if(null == fs)
		{
			ArrayList<Field> fieldList = getAllDeaclaredFields(clazz);
			if(null == fieldList)
			{
				logger.error("Failed to get fields for :" + clazz);
			}
			fs = new Field[fieldList.size()];
			fieldList.toArray(fs);
			fieldCacheTable.put(clazz, fs);
		}
		return fs;
	}

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

	public static Type getType(Class clazz)
	{
		if(reservedClassTable.containsKey(clazz))
		{
			return reservedClassTable.get(clazz);
		}
		Type ret = null;
		if(clazz.isArray())
		{
			ret = Type.ARRAY;
		}
		else if(clazz.isEnum())
		{
			ret = Type.ENUM;
		}
		else if(Proxy.isProxyClass(clazz))
		{
			ret = Type.PROXY;
		}
		else if(clazz.equals(ArrayList.class) || clazz.equals(LinkedList.class) || Throwable.class.isAssignableFrom(clazz))
		{
			ret = Type.OTHER;
		}
		else
		{
			if(null != ReflectionCache.getDefaultConstructor(clazz))
			{
				ret = Type.POJO;
			}
			else
			{
				ret = Type.OTHER;
			}
		}
		reservedClassTable.put(clazz, ret);
		return ret;
	}

	private static void getTypeList(Class clazz, List<Type> typeList)
	{
		Type type = getType(clazz);
		typeList.add(type);
		switch(type)
		{
			case POJO:
			case PROXY:
			{
				Field[] fs = getSerializableFields(clazz);
				for(Field field : fs)
				{
					getTypeList(field.getClass(), typeList);
				}
				break;
			}
			default:
				break;
		}
	}

	public static List<Type> getTypeList(Class clazz)
	{
		List<Type> ret = typeListCacheTable.get(clazz);
		if(null == ret)
		{
			ret = new ArrayList<Type>();
			typeListCacheTable.put(clazz, ret);
		}
		getTypeList(clazz, ret);
		return ret;
	}

}
