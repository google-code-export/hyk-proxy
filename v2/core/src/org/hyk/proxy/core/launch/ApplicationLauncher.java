/**
 * This file is part of the hyk-proxy-core project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ApplicationLauncher.java 
 *
 * @author yinqiwen [ 2011-11-27 | ÏÂÎç09:50:22 ]
 *
 */
package org.hyk.proxy.core.launch;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.arch.util.PropertiesHelper;
import org.hyk.proxy.core.common.Constants;
import org.hyk.proxy.core.config.DesktopCoreConfiguration;
import org.hyk.proxy.core.framework.Framework;
import org.hyk.proxy.core.plugin.DesktopPluginManager;
import org.hyk.proxy.core.trace.TUITrace;

/**
 *
 */
public class ApplicationLauncher
{
	private static void initLoggerConfig() throws IOException
	{
		String home = System.getProperty(Constants.APP_HOME);
		if (null == home)
		{
			home = ".";
		}
		String loggingCfgFile = home + "/conf/logging properties";
		FileInputStream fis = new FileInputStream(loggingCfgFile);
		Properties props = new Properties();
		props.load(fis);
		fis.close();
		if (PropertiesHelper.replaceSystemProperties(props) > 0)
		{
			FileOutputStream fos = new FileOutputStream(loggingCfgFile);
			props.store(fos, "");
			fos.close();
		}

		System.setProperty("java.util.logging.config.file", loggingCfgFile);
	}

	public static void main(String[] args) throws IOException
	{
		initLoggerConfig();

		Framework fr = null;
		if (args.length == 0 || args[0].equals("cli"))
		{
			fr = new Framework(DesktopCoreConfiguration.getInstance(),
			        DesktopPluginManager.getInstance(), new TUITrace());
		}
		fr.start();
	}
}
