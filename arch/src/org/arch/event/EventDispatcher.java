/**
 * 
 */
package org.arch.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.arch.buffer.Buffer;

/**
 * @author qiyingwang
 * 
 */
public class EventDispatcher
{
	static class RegisterValue
	{
		Class clazz;
		EventHandler handler;
	}

	private static EventDispatcher globalDispatcherInstance = new EventDispatcher();
	private Map<TypeVersion, RegisterValue> eventHandlerTable = new ConcurrentHashMap<TypeVersion, RegisterValue>();

	private EventDispatcher(){}
	
	public static EventDispatcher getSingletonInstance()
	{
		return globalDispatcherInstance;
	}
	
	public static EventDispatcher getInstance()
	{
		return new EventDispatcher();
	}
	
	public Event parse(Buffer buffer)
	{
		return null;
	}

	private TypeVersion getTypeVersion(Class clazz) throws Exception
	{
		TypeVersion key = Event.getTypeVersion(clazz);
		if(null == key)
		{
			throw new Exception("Invalid Event class:" + clazz.getName());
		}
		return key;
	}
	
	public void dispatch(Event event) throws Exception
	{
		Class clazz = event.getClass();
		TypeVersion key = getTypeVersion(clazz);
		RegisterValue value = eventHandlerTable.get(key);
		if(null == value)
		{
			throw new Exception("No handler can handle this event");
		}
		EventHandler handler = value.handler;
		EventHeader header = new EventHeader(key, event.getHash());
		handler.onEvent(header, event);
	}

	public void register(Class<? extends Event> clazz, EventHandler handler)
	        throws Exception
	{
		if (!Event.class.isAssignableFrom(clazz))
		{
			throw new Exception(clazz.getName() + " is invalid event class.");
		}
		TypeVersion key = getTypeVersion(clazz);

		if (eventHandlerTable.containsKey(key))
		{
			throw new Exception("Duplicate entry key for event class:"
			        + clazz.getName());
		}
		RegisterValue value = new RegisterValue();
		value.clazz = clazz;
		value.handler = handler;
		eventHandlerTable.put(key, value);
	}

}
