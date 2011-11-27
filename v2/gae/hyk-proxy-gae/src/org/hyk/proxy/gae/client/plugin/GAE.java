package org.hyk.proxy.gae.client.plugin;

import org.arch.event.EventDispatcher;
import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.core.plugin.Plugin;
import org.hyk.proxy.gae.client.handler.ClientProxyEventHandler;
import org.hyk.proxy.gae.common.GAEPluginVersion;

public class GAE implements Plugin
{
	ClientProxyEventHandler handler = new ClientProxyEventHandler();
	@Override
	public String getName()
	{
		return "GAE";
	}

	@Override
	public String getVersion()
	{
		return GAEPluginVersion.value;
	}

	@Override
	public void load() throws Exception
	{

	}

	@Override
	public void active() throws Exception
	{
		EventDispatcher.getSingletonInstance().register(HTTPRequestEvent.class, handler);
		EventDispatcher.getSingletonInstance().register(HTTPChunkEvent.class, handler);
	}

	@Override
	public void deactive() throws Exception
	{

	}

	@Override
	public void unload() throws Exception
	{

	}
}
