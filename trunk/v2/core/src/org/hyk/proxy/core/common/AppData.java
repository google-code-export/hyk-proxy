/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppData.java 
 *
 * @author yinqiwen [ 2010-8-28 | 11:28:59 PM]
 *
 */
package org.hyk.proxy.core.common;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class AppData
{
	protected static Logger logger = LoggerFactory.getLogger(AppData.class);

	private static File APP_PLUGINS = null;
	private static File APP_HOME = null;
	private static File APP_USER_HOME = null;
	private static File USR_PLUGINS_STATE = null;

	private static File FAKE_CERT_HOME = null;
	static
	{
		init();
	}

	private static void init()
	{
		try
		{
			String filesp = System.getProperty("file.separator");
			String usrHome = System.getProperty("user.home");
			String appHome = System.getProperty(Constants.APP_HOME);
			if(null == appHome)
			{
				appHome = ".";
			}
			APP_HOME = new File(appHome);
			APP_USER_HOME = new File(usrHome, ".hyk-proxy-v2");
			if(!APP_USER_HOME.exists())
			{
				APP_USER_HOME.mkdir();
			}
			USR_PLUGINS_STATE = new File(APP_USER_HOME, ".plugins");
			if(!USR_PLUGINS_STATE.exists())
			{
				USR_PLUGINS_STATE.createNewFile();
			}
			FAKE_CERT_HOME = new File(appHome + filesp
			        + "conf");
			APP_PLUGINS = new File(APP_HOME, "plugins");
			if (!APP_PLUGINS.exists())
			{
				APP_PLUGINS.mkdir();
			}
			if (!FAKE_CERT_HOME.exists())
			{
				try
				{
					FAKE_CERT_HOME.mkdir();
				}
				catch (Exception e)
				{
					FAKE_CERT_HOME = null;
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to init appdata.", e);
		}

	}

	public static File GetFakeSSLCertHome()
	{
		return FAKE_CERT_HOME;
	}

	public static File getPluginsHome()
	{
		return APP_PLUGINS;
	}

	public static File getUserPluginState()
	{
		return USR_PLUGINS_STATE;
	}

}
