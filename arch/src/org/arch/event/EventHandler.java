/**
 * 
 */
package org.arch.event;


/**
 * @author qiyingwang
 *
 */
public interface EventHandler
{
	public void onEvent(EventHeader header, Event event);
}
