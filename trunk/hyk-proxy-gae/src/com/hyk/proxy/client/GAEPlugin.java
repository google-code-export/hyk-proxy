/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: GAEPlugin.java 
 *
 * @author yinqiwen [ 2010-8-14 | 09:55:35 PM ]
 *
 */
package com.hyk.proxy.client;

import java.io.File;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.hyk.proxy.client.application.gae.admin.Admin;
import com.hyk.proxy.client.application.gae.event.GoogleAppEngineHttpProxyEventServiceFactory;
import com.hyk.proxy.client.application.gae.gui.GAEConfigPanel;
import com.hyk.proxy.client.util.GAEImageUtil;
import com.hyk.proxy.client.util.GoogleAvailableService;
import com.hyk.proxy.common.ExtensionsLauncher;
import com.hyk.proxy.framework.common.Constants;
import com.hyk.proxy.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.framework.plugin.GUIPlugin;
import com.hyk.proxy.framework.plugin.PluginAdmin;
import com.hyk.proxy.framework.plugin.PluginContext;


/**
 *
 */
public class GAEPlugin implements GUIPlugin
{

	private static File home = null;
	
	public static File getHome()
	{
		return home;
	}
	
	@Override
	public void onLoad(PluginContext context) throws Exception
	{
		home = context.getHome();
	}


	@Override
	public void onActive(PluginContext context) throws Exception
	{
		GoogleAvailableService.getInstance();
		ExtensionsLauncher.init();
		HttpProxyEventServiceFactory.Registry.register(new GoogleAppEngineHttpProxyEventServiceFactory());

	}

	@Override
	public void onUnload(PluginContext context) throws Exception
	{

	}

	@Override
	public void onDeactive(PluginContext context) throws Exception
	{

	}

	@Override
	public JPanel getConfigPanel()
	{
		return new GAEConfigPanel();
	}


	@Override
    public ImageIcon getIcon()
    {
	    return GAEImageUtil.APPENGINE;
    }

	@Override
    public PluginAdmin getAdmin()
    {
	    return new Admin();
    }

}
