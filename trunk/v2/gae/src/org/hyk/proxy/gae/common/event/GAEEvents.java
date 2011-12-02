/**
 * 
 */
package org.hyk.proxy.gae.common.event;

import org.arch.event.EventDispatcher;
import org.arch.event.EventHandler;
import org.arch.event.EventSegment;

/**
 * @author qiyingwang
 *
 */
public class GAEEvents
{
	public static void init(EventHandler handler)
	{
		try
        {
	        EventDispatcher.getSingletonInstance().register(AuthResponseEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(AuthRequestEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(AdminResponseEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(BlackListOperationEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(GroupOperationEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(ListGroupRequestEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(ListGroupResponseEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(ListUserRequestEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(ListUserResponseEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(UserOperationEvent.class, handler);
	        EventDispatcher.getSingletonInstance().register(EventSegment.class, handler);
	        EventDispatcher.getSingletonInstance().register(ServerConfigRequestEvent.class, handler);
        }
        catch (Exception e)
        {
	       //
        }
		
	}
}
