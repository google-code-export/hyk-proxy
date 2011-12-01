/**
 * 
 */
package org.hyk.proxy.gae.client.handler;

import org.arch.event.Event;
import org.arch.event.EventHandler;
import org.arch.event.EventHeader;
import org.arch.event.NamedEventHandler;
import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPConnectionEvent;
import org.arch.event.http.HTTPEventContants;
import org.arch.event.http.HTTPRequestEvent;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 *
 */
public class ClientProxyEventHandler implements NamedEventHandler
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
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
			case HTTPEventContants.HTTP_CHUNK_EVENT_TYPE:
			{
				handleChunk((HTTPChunkEvent) event, (Channel) event.getAttachment());
				break;
			}
			case HTTPEventContants.HTTP_CONNECTION_EVENT_TYPE:
			{
				HTTPConnectionEvent ev = (HTTPConnectionEvent) event;
				sessionManager.handleConnectionEvent(ev);
				break;
			}
			default:
			{
				logger.error("Unexpected event type:" + header.type);
				break;
			}
		}
    }

	@Override
    public String getName()
    {
	    return "GAE";
    }
}
