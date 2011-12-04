package org.hyk.proxy.gae.client.plugin;

import org.hyk.proxy.core.plugin.Plugin;
import org.hyk.proxy.core.plugin.PluginContext;
import org.hyk.proxy.gae.client.admin.GAEAdmin;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.ProxyInfo;
import org.hyk.proxy.gae.client.connection.ProxyConnectionManager;
import org.hyk.proxy.gae.client.handler.ClientProxyEventHandler;
import org.hyk.proxy.gae.common.event.GAEEvents;

public class GAE implements Plugin
{
	ClientProxyEventHandler handler = new ClientProxyEventHandler();


	@Override
	public void onLoad(PluginContext context) throws Exception
	{

	}

	
	private void initProxyConnections()
	{
		if(!ProxyConnectionManager.getInstance().init())
		{
			ProxyInfo info = GAEClientConfiguration.getInstance().getLocalProxy();
			if(null == info || info.host == null)
			{
				//try google cn
			}
			else if(1==1)
			{
				
			}
			else
			{
				//try google https
			}
		}
	}
	
	@Override
	public void onActive(PluginContext context) throws Exception
	{
		ClientProxyEventHandler handler = new ClientProxyEventHandler();
		GAEEvents.init(handler, false);
		initProxyConnections();
	}

	@Override
	public void onDeactive(PluginContext context) throws Exception
	{

	}

	@Override
	public void onUnload(PluginContext context) throws Exception
	{

	}

	@Override
    public Runnable getAdminInterface()
    {
	    return new GAEAdmin();
    }
}
