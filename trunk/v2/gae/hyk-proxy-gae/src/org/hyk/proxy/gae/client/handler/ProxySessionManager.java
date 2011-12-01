/**
 * 
 */
package org.hyk.proxy.gae.client.handler;

import java.util.HashMap;
import java.util.Map;

import org.arch.common.Pair;
import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPConnectionEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.gae.client.handler.ProxySession;
import org.jboss.netty.channel.Channel;

/**
 * @author qiyingwang
 *
 */
public class ProxySessionManager
{
	private static ProxySessionManager instance = new ProxySessionManager();
	
	private Map<Integer, ProxySession> sessionTable = new HashMap<Integer, ProxySession>(); 
	
	public static ProxySessionManager getInstance()
	{
		return instance;
	}
	
	public void removeSession(ProxySession session)
	{
		sessionTable.remove(session.getSessionID());
	}
	
	public ProxySession getProxySession(Integer sessionID)
	{
		synchronized (sessionTable)
        {
	        return sessionTable.get(sessionID);
        }
	}
	private ProxySession createSession(Integer id, Channel ch)
	{
		ProxySession session = getProxySession(id);
		if(null == session)
		{
			session = new ProxySession(id, ch);
			synchronized (sessionTable)
            {
				sessionTable.put(id, session);
            }
		}
		return session;
	}
	
	public ProxySession handleConnectionEvent(HTTPConnectionEvent event)
	{
		Pair<Channel, Integer> attach = (Pair<Channel, Integer>) event.getAttachment();
		Channel localChannel = attach.first;
		Integer handleID = attach.second;
		ProxySession session = getProxySession(handleID);
		if(null != session)
		{
			if(event.status == HTTPConnectionEvent.CLOSED)
			{
				session.close();
			}
		}
		return session;
	}
	
	public ProxySession handleRequest(HTTPRequestEvent event)
	{
		Pair<Channel, Integer> attach = (Pair<Channel, Integer>) event.getAttachment();
		Channel localChannel = attach.first;
		Integer handleID = attach.second;
		ProxySession session = getProxySession(handleID);
		if(null == session)
		{
			session = createSession(handleID, localChannel);
		}
		session.handle(event);
		return session;
	}
	
	public ProxySession handleChunk(HTTPChunkEvent event)
	{
		Pair<Channel, Integer> attach = (Pair<Channel, Integer>) event.getAttachment();
		//Channel localChannel = attach.first;
		Integer handleID = attach.second;
		ProxySession session = getProxySession(handleID);
		if(null != session)
		{
			session.handle(event);
		}
		return session;
	}
}
