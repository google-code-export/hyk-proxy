/**
 * 
 */
package com.hyk.proxy.plugin.seattle;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.framework.plugin.PluginAdmin;
import com.hyk.proxy.framework.plugin.PluginContext;
import com.hyk.proxy.framework.plugin.TUIPlugin;
import com.hyk.proxy.plugin.seattle.event.SeattleProxyEventServiceFactory;

/**
 * @author wqy
 * 
 */
public class SeattlePlugin implements TUIPlugin
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private PluginContext context;
	@Override
	public void onLoad(PluginContext context) throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onActive(PluginContext context) throws Exception
	{
		this.context = context;
		HttpProxyEventServiceFactory.Registry
		        .register(new SeattleProxyEventServiceFactory());

	}

	@Override
	public void onUnload(PluginContext context) throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeactive(PluginContext context) throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
    public void onConfig()
    {
		File home = context.getHome();
		try
        {
	        Desktop.getDesktop().open(new File(home, "deploy"));
	        Desktop.getDesktop().open(new File(home, "etc"));
        }
        catch (IOException e)
        {
        	logger.error("Failed.", e);
        }
    }

	@Override
    public PluginAdmin getAdmin()
    {
	    return null;
    }
}
