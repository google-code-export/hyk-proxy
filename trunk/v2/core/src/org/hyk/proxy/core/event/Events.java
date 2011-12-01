/**
 * 
 */
package org.hyk.proxy.core.event;

import org.arch.event.Event;
import org.arch.event.EventDispatcher;
import org.arch.event.EventHandler;
import org.arch.event.EventHeader;
import org.arch.event.EventSegment;
import org.arch.event.NamedEventHandler;
import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPConnectionEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.core.config.CoreConfiguration;

/**
 * @author qiyingwang
 *
 */
public class Events
{
	
	public static class NameDispatchEventHandler implements EventHandler
	{
		CoreConfiguration config;
		public NameDispatchEventHandler(CoreConfiguration config)
		{
			this.config = config;
		}
		@Override
        public void onEvent(EventHeader header, Event event)
        {
			String name = config.getProxyEventHandler();
	        NamedEventHandler handler = EventDispatcher.getSingletonInstance().getNamedEventHandler(name);
	        if(null != handler)
	        {
	        	handler.onEvent(header, event);
	        }
	        else
	        {
	        	
	        }
	        
        }
		
	}
	public static void init(CoreConfiguration config)
	{
		try
        {
			NameDispatchEventHandler handler = new NameDispatchEventHandler(config);
	        EventDispatcher.getSingletonInstance().register(HTTPRequestEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(HTTPChunkEvent.class, handler);
			EventDispatcher.getSingletonInstance().register(HTTPConnectionEvent.class, handler);
			EventDispatcher.getSingletonInstance().register(EventSegment.class, handler);
        }
        catch (Exception e)
        {
	        //
        }	
	}
}
