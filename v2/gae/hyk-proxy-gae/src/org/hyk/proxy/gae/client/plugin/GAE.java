package org.hyk.proxy.gae.client.plugin;

import org.arch.event.EventDispatcher;
import org.arch.event.EventSegment;
import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.core.plugin.Plugin;
import org.hyk.proxy.core.plugin.PluginContext;
import org.hyk.proxy.gae.client.connection.ProxyConnectionManager;
import org.hyk.proxy.gae.client.handler.ClientProxyEventHandler;
import org.hyk.proxy.gae.common.GAEPluginVersion;
import org.hyk.proxy.gae.common.event.AuthResponseEvent;

public class GAE implements Plugin
{
	ClientProxyEventHandler handler = new ClientProxyEventHandler();


	@Override
	public void onLoad(PluginContext context) throws Exception
	{

	}

	@Override
	public void onActive(PluginContext context) throws Exception
	{
		EventDispatcher.getSingletonInstance().register(HTTPRequestEvent.class, handler);
		EventDispatcher.getSingletonInstance().register(HTTPChunkEvent.class, handler);
		EventDispatcher.getSingletonInstance().register(EventSegment.class, handler);
		EventDispatcher.getSingletonInstance().register(AuthResponseEvent.class, handler);
		ProxyConnectionManager.getInstance().init();
	}

	@Override
	public void onDeactive(PluginContext context) throws Exception
	{

	}

	@Override
	public void onUnload(PluginContext context) throws Exception
	{

	}
}
