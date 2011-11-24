/**
 * 
 */
package org.hyk.proxy.gae.client.connection.http;

import org.arch.event.Event;

/**
 * @author qiyingwang
 *
 */
public class HTTPConnection
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
