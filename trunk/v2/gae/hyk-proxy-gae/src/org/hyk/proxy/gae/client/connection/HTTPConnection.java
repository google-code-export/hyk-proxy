/**
 * 
 */
package org.hyk.proxy.gae.client.connection;

import org.arch.event.Event;
import org.hyk.proxy.gae.client.connection.ClientConnection;

/**
 * @author qiyingwang
 *
 */
public class HTTPConnection implements ClientConnection
{
	private boolean waitingResponse = false;
	
	public boolean send(Event event)
	{
		if(waitingResponse)
		{
			return false;
		}
		
		return true;
	}
}
