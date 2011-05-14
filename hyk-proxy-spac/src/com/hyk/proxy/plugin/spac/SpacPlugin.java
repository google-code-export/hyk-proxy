/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Spac.java 
 *
 * @author yinqiwen [ 2010-6-14 | 08:57:05 PM ]
 *
 */
package com.hyk.proxy.plugin.spac;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tykedog.csl.interpreter.CSL;

import com.hyk.proxy.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.framework.plugin.PluginAdmin;
import com.hyk.proxy.framework.plugin.PluginContext;
import com.hyk.proxy.framework.plugin.TUIPlugin;
import com.hyk.proxy.plugin.spac.event.SpacProxyEventServiceFactory;
import com.hyk.proxy.plugin.spac.event.forward.DirectProxyEventServiceFactory;
import com.hyk.proxy.plugin.spac.event.forward.ForwardProxyEventServiceFactory;
import com.hyk.proxy.plugin.spac.script.CSLApiImpl;
import com.hyk.proxy.plugin.spac.script.Commands;

/**
 *
 */
public class SpacPlugin implements TUIPlugin
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	PluginContext context;

	@Override
	public void onLoad(PluginContext context) throws Exception
	{
		this.context = context;
	}

	@Override
	public void onActive(PluginContext context) throws Exception
	{
		CSL csl = CSL.Builder
		        .build(getClass().getResourceAsStream("/spac.csl"));
		csl.setCalculator(new CSLApiImpl());
		csl.setComparator(new CSLApiImpl());
		csl.addFunction(Commands.INT);
		csl.addFunction(Commands.GETHEADER);
		csl.addFunction(Commands.PRINT);
		csl.addFunction(Commands.GETRESCODE);
		csl.addFunction(Commands.SYSTEM);
		csl.addFunction(Commands.LOG);
		HttpProxyEventServiceFactory.Registry
		        .register(new SpacProxyEventServiceFactory(csl));
		HttpProxyEventServiceFactory.Registry
		        .register(new DirectProxyEventServiceFactory());

		final CSL tmp = csl;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				long waittime = 10*1000;
				while(true)
				{
					try
                    {
	                    Thread.sleep(waittime);
	                    Integer nextwait = (Integer) tmp.invoke("onRoutine", null);
	                    waittime = nextwait.longValue();
	                    if(waittime < 0)
	                    {
	                    	return;
	                    }
	                    waittime *= 1000;
                    }
                    catch (Exception e)
                    {
	                    logger.error("", e);
                    }
					
				}

				
			}
		}).start();
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
			Desktop.getDesktop().browse((new File(home, "etc").toURI()));
			// Desktop.getDesktop().edit(new File(home, "etc/spac.csl"));
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
