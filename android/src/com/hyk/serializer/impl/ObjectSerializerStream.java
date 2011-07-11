/**
 * 
 */
package com.hyk.serializer.impl;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.rpc.core.constant.RpcConstants;
import com.hyk.serializer.Externalizable;
import com.hyk.serializer.annotation.Stream;
import com.hyk.serializer.reflect.ReflectionCache;
import com.hyk.serializer.util.ContextUtil;
import com.hyk.util.thread.ThreadLocalUtil;

/**
 * @author qiying.wang
 * 
 */
public class ObjectSerializerStream<T> extends SerailizerStream<T>
{	
	public static class ResortFieldIndicator
	{
		public boolean resort;
	}
	
	static class FieldComarator implements Comparator<Field>
	{
		@Override
        public int compare(Field object1, Field object2)
        {
	        return object1.getName().compareTo(object2.getName());
        }
		
	}
	@Override
	protected T unmarshal(Class<T> type, ChannelDataBuffer data) throws NotSerializableException, IOException, InstantiationException
	{
		try
		{
			T ret = (T)ReflectionCache.getDefaultConstructor(type).newInstance(null);
			
			if(!(ret instanceof Serializable))
			{
				throw new NotSerializableException(type.getName());
			}
			ContextUtil.addDeserializeThreadLocalObject(ret);
			if (ret instanceof Externalizable) {
				Externalizable externalizable = (Externalizable) ret;
				externalizable.readExternal(new Input(data));
				return ret;
			}
			Field[] fs = ReflectionCache.getSerializableFields(type);
			ResortFieldIndicator indicator =  ThreadLocalUtil.getThreadLocalUtil(ResortFieldIndicator.class).getThreadLocalObject();
			String indicatorstr = System.getProperty(RpcConstants.SERIALIZE_REFLECTIOON_SORT_FIELD);
			if((null != indicator && indicator.resort) || null != indicatorstr)
			{
				Arrays.sort(fs, new FieldComarator());
			}
			
			while(true)
			{
				int tag = readTag(data);
				if(tag == 0)
					break;
				Field f = fs[tag - 1];
				Class fieldType = f.getType();
				if(f.isAnnotationPresent(Stream.class))
				{
					
				}
				f.set(ret, readObject(data, fieldType));
			}
			return ret;
		}
		catch(IllegalAccessException e)
		{
			throw new IOException(e.getMessage());
		}
		catch(InvocationTargetException e)
		{
			throw new IOException(e.getMessage());
		}
	}

	@Override
	protected ChannelDataBuffer marshal(T value, ChannelDataBuffer data) throws NotSerializableException, IOException
	{	
		Class clazz = value.getClass();
		if(!(value instanceof Serializable))
		{
			throw new NotSerializableException(clazz.getName());
		}
		if (value instanceof Externalizable) {
			Externalizable externalizable = (Externalizable) value;
			externalizable.writeExternal(new Output(data));
			return data;
		}
	
		try
		{
			//writeInt(data, 0);
			Field[] fs = ReflectionCache.getSerializableFields(clazz);
			ResortFieldIndicator indicator =  ThreadLocalUtil.getThreadLocalUtil(ResortFieldIndicator.class).getThreadLocalObject();
			String indicatorstr = System.getProperty(RpcConstants.SERIALIZE_REFLECTIOON_SORT_FIELD);
			if((null != indicator && indicator.resort) || null != indicatorstr)
			{
				Arrays.sort(fs, new FieldComarator());
			}
			for(int i = 0; i < fs.length; i++)
			{
				Field f = fs[i];
				Object fieldValue = f.get(value);
				if(null != fieldValue)
				{
					writeTag(data, i + 1);
					writeObject(data, fieldValue, f.getType());
				}
				
			}
			writeTag(data, 0);
		}
		catch(IllegalAccessException e)
		{
			throw new IOException(e.getMessage());
		}

		return data;
	}

}
