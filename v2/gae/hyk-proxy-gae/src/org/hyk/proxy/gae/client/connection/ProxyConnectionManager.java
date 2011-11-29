/**
 * 
 */
package org.hyk.proxy.gae.client.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.arch.event.Event;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.util.ListSelector;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.GAEServerAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author qiyingwang
 * 
 */
public class ProxyConnectionManager
{
	private static ProxyConnectionManager instance = new ProxyConnectionManager();
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private Map<String, List<ProxyConnection> > conntionTable = new HashMap<String, List<ProxyConnection>>();
	
	private ListSelector<GAEServerAuth> seletor = null;

	public static ProxyConnectionManager getInstance()
	{
		return instance;
	}

	public boolean init()
	{
		List<GAEServerAuth> auths = new ArrayList<GAEClientConfiguration.GAEServerAuth>();
		Collections.copy(auths, GAEClientConfiguration.getInstance().getGAEServerAuths());
		for(GAEServerAuth auth:GAEClientConfiguration.getInstance().getGAEServerAuths())
		{
			ProxyConnection conn = getClientConnection(auth);
			if(null == conn)
			{
				logger.error("Failed to auth connetion for appid:" + auth.appid);
				auths.remove(auth);
			}
		}
		if(auths.isEmpty())
		{
			logger.error("Failed to connect remote GAE server.");
			return false;
		}
		if(logger.isInfoEnabled())
		{
			int size =  auths.size();
			logger.info("Success to connect " + size + "GAE server" + (size > 1 ?"s":""));
		}
		seletor = new ListSelector<GAEServerAuth>(auths);
		return true;
	}
	
	private ProxyConnection getClientConnection(GAEServerAuth auth)
	{
		ProxyConnection connection = null;
		List<ProxyConnection> connlist = conntionTable.get(auth.appid);
		if(null == connlist)
		{
			connlist = new ArrayList<ProxyConnection>();
			conntionTable.put(auth.appid, connlist);
		}
		for(ProxyConnection conn:connlist)
		{
			if(conn.isReady())
			{
				return conn;
			}
		}
		if(connlist.size() >= GAEClientConfiguration.getInstance().getConnectionPoolSize())
		{
			return connlist.get(0);
		}
		
		switch (GAEClientConfiguration.getInstance().getConnectionMode())
		{
			case HTTP:
			case HTTPS:
			{
				connection = new HTTPProxyConnection(auth);	
				break;
			}
			case XMPP:
			{
				break;
			}
			default:
			{
				break;
			}
		}
		if(null != connection)
		{
			if(!connlist.isEmpty())
			{
				connection.setAuthToken(connlist.get(0).getAuthToken());
			}
			else
			{
				connection.auth();
			}
			connlist.add(connection);
		}
		return connection;
	}

	public ProxyConnection getClientConnection(HTTPRequestEvent event)
	{
		String appid = GAEClientConfiguration.getInstance()
		        .getBindingAppId(event.getHeader("Host"));
		GAEServerAuth auth = null;
		if (null == appid)
		{
			auth = seletor.select();
		}
		else
		{
			auth = GAEClientConfiguration.getInstance().getGAEServerAuth(appid);
		}
		return getClientConnection(auth);
	}

	public void routine()
	{

	}

}
