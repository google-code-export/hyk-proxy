/**
 * 
 */
package org.arch.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;

/**
 * @author qiyingwang
 *
 */
public abstract class Event
{
	public static final int RESERVED_SEGMENT_EVENT_TYPE = 48100;
	private static Map<Class, TypeVersion> typeVerTable = new ConcurrentHashMap<Class, TypeVersion>();
	private int hash;
	private Object attachment;
	
	public static synchronized TypeVersion getTypeVersion(Class<? extends Event> clazz)
	{
		TypeVersion typever = typeVerTable.get(clazz);
		if(null == typever)
		{
			EventType type = clazz.getAnnotation(EventType.class);
			EventVersion ver = clazz.getAnnotation(EventVersion.class);
			if(null == type || null == ver)
			{
				return null;
			}
			typever = new TypeVersion();
			typever.type = type.value();
			typever.version = ver.value();
			typeVerTable.put(clazz, typever);
		}
		
		return typever;
	}
	
	public void setHash(int hash)
	{
		this.hash = hash;
	}
	public int getHash()
	{
		return hash;
	}
	public Object getAttachment()
	{
		return attachment;
	}
	public void setAttachment(Object obj)
	{
		attachment = obj;
	}
	protected abstract boolean onDecode(Buffer buffer);
	protected abstract boolean onEncode(Buffer buffer);
	
	public static EventHeader peekHeader(Buffer buffer)
	{
		EventHeader header = new EventHeader();
		int pos = buffer.getReadIndex();
		if(!header.decode(buffer))
		{
			header = null;
		}
		buffer.setReadIndex(pos);
		return header;
	}
	
	public final boolean encode(Buffer buffer)
	{
		TypeVersion typever = getTypeVersion(this.getClass());
		EventHeader header = new EventHeader(typever, hash);
		header.encode(buffer);
		return onEncode(buffer);
	}
	
	public final boolean decode(Buffer buffer, boolean hasHeader)
	{
		if(hasHeader)
		{
			try
            {
				EventHeader header = new EventHeader();
				if(!header.decode(buffer))
				{
					return false;
				}
				TypeVersion typever = getTypeVersion(this.getClass());
				if(header.type != typever.type || header.version != typever.version)
				{
					return false;
				}
				setHash(header.hash);
            }
            catch (Exception e)
            {
	            return false;
            }
			
		}
		return onDecode(buffer);
	}
}
