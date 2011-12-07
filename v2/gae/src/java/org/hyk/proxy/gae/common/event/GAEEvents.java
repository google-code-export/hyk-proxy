/**
 * 
 */
package org.hyk.proxy.gae.common.event;

import org.arch.event.EventDispatcher;
import org.arch.event.EventHandler;
import org.arch.event.EventSegment;
import org.arch.event.http.HTTPErrorEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;

/**
 * @author qiyingwang
 * 
 */
public class GAEEvents
{
	public static void init(EventHandler handler, boolean isServer)
	{
		try
		{
			EventDispatcher.getSingletonInstance().register(
			        HTTPResponseEvent.class, handler);
			EventDispatcher.getSingletonInstance().register(
			        HTTPErrorEvent.class, handler);
			EventDispatcher.getSingletonInstance().register(
			        AuthResponseEvent.class, handler);
			EventDispatcher.getSingletonInstance().register(
			        AdminResponseEvent.class, handler);
			EventDispatcher.getSingletonInstance().register(
			        ListGroupResponseEvent.class, handler);

			EventDispatcher.getSingletonInstance().register(
			        ListUserResponseEvent.class, handler);

			EventDispatcher.getSingletonInstance().register(EventSegment.class,
			        handler);

			EventDispatcher.getSingletonInstance().register(
			        CompressEvent.class, handler);
			EventDispatcher.getSingletonInstance().register(EncryptEvent.class,
			        handler);
			EventDispatcher.getSingletonInstance().register(
			        ServerConfigEvent.class, handler);
			EventDispatcher.getSingletonInstance().register(
			        RequestSharedAppIDResultEvent.class, handler);
			EventDispatcher.getSingletonInstance().register(
			        RequestSharedAppIDEvent.class, handler);
			if (isServer)
			{
				EventDispatcher.getSingletonInstance().register(
				        AuthRequestEvent.class, handler);
				EventDispatcher.getSingletonInstance().register(
				        HTTPRequestEvent.class, handler);
				EventDispatcher.getSingletonInstance().register(
				        BlackListOperationEvent.class, handler);
				EventDispatcher.getSingletonInstance().register(
				        GroupOperationEvent.class, handler);
				EventDispatcher.getSingletonInstance().register(
				        ListGroupRequestEvent.class, handler);

				EventDispatcher.getSingletonInstance().register(
				        ListUserRequestEvent.class, handler);
				EventDispatcher.getSingletonInstance().register(
				        UserOperationEvent.class, handler);

			}

		}
		catch (Exception e)
		{
			//
		}

	}
}
