/**
 * 
 */
package org.hyk.proxy.gae.client.handler;

import java.util.HashMap;
import java.util.Map;

import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.gae.client.handler.ProxySession;
import org.jboss.netty.channel.Channel;

/**
 * @author qiyingwang
 *
 */
public class ProxySessionManager
{
	private Map<Integer, ProxySession> sessionTable = new HashMap<Integer, ProxySession>(); 
	private ProxySession getProxySession(int sessionID)
	{
		synchronized (sessionTable)
        {
	        return sessionTable.get(sessionID);
        }
	}
	private ProxySession createSession(Channel ch)
	{
		ProxySession session = getProxySession(ch.getId());
		if(null == session)
		{
			session = new ProxySession(ch);
			synchronized (sessionTable)
            {
				sessionTable.put(ch.getId(), session);
            }
		}
		return session;
	}
	
	public ProxySession handleRequest(HTTPRequestEvent event)
	{
		Channel localChannel = (Channel) event.getAttachment();
		Integer channelID = localChannel.getId();
		ProxySession session = getProxySession(channelID);
		if(null == session)
		{
			session = createSession(localChannel);
		}
		session.handle(event);
		return session;
	}
	
	public ProxySession handleChunk(HTTPChunkEvent event)
	{
		Channel localChannel = (Channel) event.getAttachment();
		Integer channelID = localChannel.getId();
		ProxySession session = getProxySession(channelID);
		if(null != session)
		{
			session.handle(event);
		}
		return session;
	}
}
