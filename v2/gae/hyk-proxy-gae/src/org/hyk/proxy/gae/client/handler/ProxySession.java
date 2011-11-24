/**
 * 
 */
package org.hyk.proxy.gae.client.handler;

import java.util.HashMap;
import java.util.Map;

import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;
import org.hyk.proxy.gae.client.connection.http.HTTPConnectionManager;

/**
 * @author qiyingwang
 *
 */
public class ProxySession implements Runnable
{
	private static Map<Integer, ProxySession> sessionTable = new HashMap<Integer, ProxySession>();
	private HTTPConnectionManager connManager = null;
	
	
	public static ProxySession getSession(int sessionID)
	{
		return null;
	}
	public void handle(HTTPRequestEvent event)
	{
		
	}
	public void handle(HTTPResponseEvent event)
	{
		
	}

	@Override
    public void run()
    {
	        
    }
}
