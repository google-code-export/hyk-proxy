/**
 * 
 */
package org.hyk.proxy.gae.client.handler;

import org.arch.event.Event;
import org.arch.event.EventHandler;
import org.arch.event.EventHeader;
import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPEventContants;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.gae.client.connection.http.HTTPConnectionManager;

/**
 * @author qiyingwang
 *
 */
public class ProxyEventHandler implements EventHandler
{
	private void handleRequest(HTTPRequestEvent event)
	{
		new ProxySession().handle(event);
	}
	
	private void handleChunk(HTTPChunkEvent event)
	{
		int sessionID = -1;
		//ProxySession.getSession(sessionID).handle(null);
	}
	
	
	@Override
    public void onEvent(EventHeader header, Event event)
    {
	    switch (header.type)
        {
			case HTTPEventContants.HTTP_REQUEST_EVENT_TYPE:
			{
				handleRequest((HTTPRequestEvent) event);
				break;
			}
			case HTTPEventContants.HTTP_RESPONSE_EVENT_TYPE:
			{
				break;
			}
			case HTTPEventContants.HTTP_CHUNK_EVENT_TYPE:
			{
				handleChunk((HTTPChunkEvent) event);
				break;
			}
			default:
			{
				break;
			}
		}
    }
}
