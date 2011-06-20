/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: SerializerImplFactory.java 
 *
 * @author qiying.wang [ Jan 21, 2010 | 11:25:13 AM ]
 *
 */
package com.hyk.serializer.impl;
import com.hyk.serializer.Serializer;
import com.hyk.serializer.StandardSerializer;
import com.hyk.serializer.io.Type;
import com.hyk.serializer.reflect.ReflectionCache;

/**
 *
 */
public class SerailizerStreamFactory
{
	static IntSerializerStream intSerializer = new IntSerializerStream();
	static ShortSerializerStream shortSerializer = new ShortSerializerStream();
	static ByteSerializerStream byteSerializer = new ByteSerializerStream();
	static CharSerializerStream charSerializer = new CharSerializerStream();
	static BooleanSerializerStream boolSerializer = new BooleanSerializerStream();
	static LongSerializerStream longSerializer = new LongSerializerStream();
	static FloatSerializerStream floatSerializer = new FloatSerializerStream();
	static DoubleSerializerStream doubleSerializer = new DoubleSerializerStream();
	static StringSerializerStream stringSerializer = new StringSerializerStream();
	static EnumSerializerStream enumSerializer = new EnumSerializerStream();
	static ArraySerializerStream arraySerializer = new ArraySerializerStream();
	static ObjectSerializerStream objectSerializer = new ObjectSerializerStream();
	static ProxySerializerStream proxySerializer = new ProxySerializerStream();
	public static OtherSerializerStream otherSerializer = new OtherSerializerStream();
	
	public static  SerailizerStream getSerializer(Class clazz)
	{
		Type type = ReflectionCache.getType(clazz);
		return getSerializer(type);
	}
	
	public static  SerailizerStream getSerializer(Type type)
	{
		//Type type = ReflectionCache.getType(clazz);
		switch(type)
		{
			case BYTE:
			{
				return byteSerializer;
			}
			case CHAR:
			{
				return charSerializer;
			}
			case BOOL:
			{
				return boolSerializer;
			}
			case ARRAY:
			{
				return arraySerializer;
			}
			case DOUBLE:
			{
				return doubleSerializer;
			}
			case FLOAT:
			{
				return floatSerializer;
			}
			case INT:
			{
				return intSerializer;
			}
			case LONG:
			{
				return longSerializer;
			}
			case SHORT:
			{
				return shortSerializer;
			}
			case STRING:
			{
				return stringSerializer;
			}
			case POJO:
			{
				return objectSerializer;
			}
			case ENUM:
			{
				return enumSerializer;
			}
			case PROXY:
			{
				return proxySerializer;
			}
			case OTHER:
			{
				return otherSerializer;
			}
			
			default:
				return null;
		}
	}
}
