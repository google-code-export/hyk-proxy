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
public class HTTPConnection extends  ClientConnection
{
	private boolean waitingResponse = false;
	
	protected boolean doSend(Event event)
	{
		if(waitingResponse)
		{
			return false;
		}
		
		return true;
	}
}
