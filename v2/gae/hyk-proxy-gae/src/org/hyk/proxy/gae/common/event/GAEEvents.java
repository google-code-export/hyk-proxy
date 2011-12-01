/**
 * 
 */
package org.hyk.proxy.gae.common.event;

import org.arch.event.EventDispatcher;

/**
 * @author qiyingwang
 *
 */
public class GAEEvents
{
	public static void init()
	{
		try
        {
	        EventDispatcher.getSingletonInstance().registerEvent(AuthResponseEvent.class);
	        EventDispatcher.getSingletonInstance().registerEvent(AdminResponseEvent.class);
	        EventDispatcher.getSingletonInstance().registerEvent(AdminResponseEvent.class);
        }
        catch (Exception e)
        {
	       //
        }
		
	}
}
