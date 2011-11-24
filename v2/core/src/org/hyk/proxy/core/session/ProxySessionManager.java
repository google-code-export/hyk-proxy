/**
 * 
 */
package org.hyk.proxy.core.session;

import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.gae.client.handler.ProxySession;

/**
 * @author qiyingwang
 *
 */
public class ProxySessionManager
{
	public ProxySession getProxySession(int sessionID)
	{
		return null;
	}
	
	public ProxySession handleRequest(HTTPRequestEvent event)
	{
		return null;
	}
	
	public ProxySession handleChunk(ProxySession session, HTTPChunkEvent event)
	{
		return session;
	}
}
