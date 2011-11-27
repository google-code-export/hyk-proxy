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
import org.jboss.netty.channel.Channel;

/**
 * @author qiyingwang
 *
 */
public class ClientProxyEventHandler implements EventHandler
{
	private ProxySessionManager sessionManager = new ProxySessionManager();
	
	private void handleRequest(HTTPRequestEvent event, Channel localChannel)
	{
		sessionManager.handleRequest(event);
	}
	
	private void handleChunk(HTTPChunkEvent event, Channel localChannel)
	{
		sessionManager.handleChunk(event);
	}
	
	
	@Override
    public void onEvent(EventHeader header, Event event)
    {
	    switch (header.type)
        {
			case HTTPEventContants.HTTP_REQUEST_EVENT_TYPE:
			{
				handleRequest((HTTPRequestEvent) event, (Channel) event.getAttachment());
				break;
			}
			case HTTPEventContants.HTTP_RESPONSE_EVENT_TYPE:
			{
				break;
			}
			case HTTPEventContants.HTTP_CHUNK_EVENT_TYPE:
			{
				handleChunk((HTTPChunkEvent) event, (Channel) event.getAttachment());
				break;
			}
			default:
			{
				break;
			}
		}
    }
}
