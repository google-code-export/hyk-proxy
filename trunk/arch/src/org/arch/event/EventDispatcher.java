/**
 * 
 */
package org.arch.event;

import java.util.HashMap;
import java.util.Map;

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
	private Map<TypeVersion, RegisterValue> eventRegistry = new HashMap<TypeVersion, RegisterValue>();
	private Map<String, NamedEventHandler> eventHandlerTable = new HashMap<String, NamedEventHandler>();

	private EventDispatcher()
	{
	}

	public static EventDispatcher getSingletonInstance()
	{
		return globalDispatcherInstance;
	}

	public static EventDispatcher getInstance()
	{
		return new EventDispatcher();
	}

	public Event parse(Buffer buffer) throws Exception
	{
		EventHeader header = new EventHeader();
		if (!header.decode(buffer))
		{
			throw new Exception("[Parse]Invalid event head.");
		}
		TypeVersion key = new TypeVersion();
		key.type = header.type;
		key.version = header.version;
		RegisterValue value = eventRegistry.get(key);
		if (null == value)
		{
			throw new Exception("[Parse]No event type:version(" + key.type
			        + ":" + key.version + ") found in registry.");
		}
		return (Event) value.clazz.newInstance();
	}

	private TypeVersion getTypeVersion(Class clazz) throws Exception
	{
		TypeVersion key = Event.getTypeVersion(clazz);
		if (null == key)
		{
			throw new Exception("Invalid Event class:" + clazz.getName());
		}
		return key;
	}

	public void dispatch(Event event) throws Exception
	{
		Class clazz = event.getClass();
		TypeVersion key = getTypeVersion(clazz);
		RegisterValue value = eventRegistry.get(key);
		if (null == value)
		{
			throw new Exception("No handler can handle this event");
		}
		EventHandler handler = value.handler;
		if(null != handler)
		{
			EventHeader header = new EventHeader(key, event.getHash());
			handler.onEvent(header, event);
		}
	}

	private void register(Class<? extends Event> clazz, EventHandler handler,
	        boolean overwrite) throws Exception
	{
		if (!Event.class.isAssignableFrom(clazz))
		{
			throw new Exception(clazz.getName() + " is invalid event class.");
		}
		TypeVersion key = getTypeVersion(clazz);

		if (!overwrite && eventRegistry.containsKey(key))
		{
			throw new Exception("Duplicate entry key for event class:"
			        + clazz.getName());
		}
		RegisterValue value = new RegisterValue();
		value.clazz = clazz;
		value.handler = handler;
		eventRegistry.put(key, value);
		if (handler != null && handler instanceof NamedEventHandler)
		{
			NamedEventHandler named = (NamedEventHandler) handler;
			if (eventHandlerTable.containsKey(named.getName()))
			{
				throw new Exception("Duplicate event handler name:"
				        + named.getName());
			}
			eventHandlerTable.put(named.getName(), named);
		}
	}

	public void registerEvent(Class<? extends Event> clazz) throws Exception
	{
		register(clazz, null, true);
	}

	public void register(Class<? extends Event> clazz, EventHandler handler)
	        throws Exception
	{
		register(clazz, handler, false);
	}

	public NamedEventHandler getNamedEventHandler(String name)
	{
		return eventHandlerTable.get(name);
	}

}
