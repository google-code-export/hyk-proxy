/**
 * 
 */
package com.hyk.proxy.plugin.seattle.management;

import com.hyk.proxy.framework.management.ManageResource;
import com.hyk.proxy.framework.plugin.PluginAdmin;
import com.hyk.proxy.plugin.seattle.event.SeattleProxyEventServiceFactory;

/**
 * @author qiyingwang
 *
 */
public class SeattleManagementHandler implements ManageResource, PluginAdmin
{

	@Override
	public String handleManagementCommand(String cmd)
	{
		return null;
	}

	@Override
	public String getName()
	{
		return SeattleProxyEventServiceFactory.NAME;
	}

	@Override
	public void start()
	{
				
	}

}
