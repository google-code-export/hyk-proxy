/**
 * 
 */
package org.hyk.proxy.gae.client.connection;

import org.arch.event.Event;

/**
 * @author qiyingwang
 *
 */
public abstract class ClientConnection
{
	protected abstract boolean doSend(Event event);
	
	public boolean send(Event event)
	{
		return false;
	}
}
