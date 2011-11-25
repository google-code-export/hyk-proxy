/**
 * 
 */
package org.hyk.proxy.gae.client.connection;

import org.arch.event.Event;

/**
 * @author qiyingwang
 *
 */
public interface ClientConnection
{
	public boolean send(Event event);
}
