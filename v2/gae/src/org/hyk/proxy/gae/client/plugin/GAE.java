package org.hyk.proxy.gae.client.plugin;

import java.util.List;

import org.hyk.proxy.core.plugin.Plugin;
import org.hyk.proxy.core.plugin.PluginContext;
import org.hyk.proxy.core.util.PreferenceHelper;
import org.hyk.proxy.gae.client.admin.GAEAdmin;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.GAEServerAuth;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.ProxyInfo;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.ProxyType;
import org.hyk.proxy.gae.client.connection.ProxyConnectionManager;
import org.hyk.proxy.gae.client.handler.ClientProxyEventHandler;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.event.GAEEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GAE implements Plugin
{
	protected static Logger logger = LoggerFactory.getLogger(GAE.class);
	ClientProxyEventHandler handler = new ClientProxyEventHandler();

	@Override
	public void onLoad(PluginContext context) throws Exception
	{

	}

	public static boolean initProxyConnections(List<GAEServerAuth> auths)
	{
		if (!ProxyConnectionManager.getInstance().init(auths))
		{
			ProxyInfo info = GAEClientConfiguration.getInstance()
			        .getLocalProxy();
			if (null == info || info.host == null)
			{
				// try GoogleCN
				info = new ProxyInfo();
				info.host = GAEConstants.RESERVED_GOOGLECN_HOST_MAPPING;
				info.port = 80;
				GAEClientConfiguration.getInstance().setLocalProxy(info);
				return initProxyConnections(auths);
			}
			else if (info.host == GAEConstants.RESERVED_GOOGLECN_HOST_MAPPING)
			{
				// try GoogleHttps
				info = new ProxyInfo();
				info.host = GAEConstants.RESERVED_GOOGLECN_HOST_MAPPING;
				info.port = 443;
				info.type = ProxyType.HTTPS;
				GAEClientConfiguration.getInstance().setLocalProxy(info);
				return initProxyConnections(auths);
			}
			else
			{
				logger.error("Failed to connect GAE server.");
				return false;
			}
		}
		else
		{
			ProxyInfo info = GAEClientConfiguration.getInstance()
			        .getLocalProxy();
			PreferenceHelper.savePreference(GAEConstants.PREFERED_GOOGLE_PROXY,
			        info != null ? info.type + ":" + info.host + ":"
			                + info.port : "");
			return true;
		}
	}

	@Override
	public void onActive(PluginContext context) throws Exception
	{
		ClientProxyEventHandler handler = new ClientProxyEventHandler();
		GAEEvents.init(handler, false);
		String preferedGoogleProxy = PreferenceHelper
		        .getPreference(GAEConstants.PREFERED_GOOGLE_PROXY);
		if (null != preferedGoogleProxy
		        && !preferedGoogleProxy.trim().isEmpty())
		{
			String[] splits = preferedGoogleProxy.split(":");
			ProxyInfo info = new ProxyInfo();
			info.type = ProxyType.valueOf(splits[0].trim());
			info.host = splits[1].trim();
			info.port = Integer.parseInt(splits[2].trim());
			GAEClientConfiguration.getInstance().setLocalProxy(info);
		}
		initProxyConnections(GAEClientConfiguration.getInstance().getGAEServerAuths());
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
