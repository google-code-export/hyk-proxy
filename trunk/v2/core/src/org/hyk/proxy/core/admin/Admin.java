/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Admin.java 
 *
 * @author yinqiwen [ 2010-9-4 | 10:44:32 PM ]
 *
 */
package org.hyk.proxy.core.admin;

import java.io.Console;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hyk.proxy.core.common.Version;
import org.hyk.proxy.core.launch.ApplicationLauncher;
import org.hyk.proxy.core.plugin.DesktopPluginManager;
import org.hyk.proxy.core.plugin.DesktopPluginManager.InstalledPlugin;

/**
 *
 */
public class Admin
{

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		ApplicationLauncher.initLoggerConfig();
		DesktopPluginManager pm = DesktopPluginManager.getInstance();
		pm.loadPlugins();
		Collection<InstalledPlugin> plugins = pm.getAllInstalledPlugins();
		Console console = System.console();
		while (true)
		{
			System.out.println("==============Framework&Plugins===========");
			System.out.println("[1] Framework V" + Version.value);
			int i = 2;
			Map<Integer, InstalledPlugin> table = new HashMap<Integer, InstalledPlugin>();
			for (InstalledPlugin plugin : plugins)
			{
				table.put(i, plugin);
				System.out.println("[" + i + "] " + plugin.desc.name + " V"
						+ plugin.desc.version);
				i++;
			}
			//System.out.println("[" + i + "] Stop hyk-proxy V" + Version.value);
			System.out.println("[0] Exit");
			System.out.print("Please enter 0-" + i + ":");
			String s = console.readLine();
			try
			{
				int choice = Integer.parseInt(s);
				if (choice >= 0 && choice <= i)
				{
					if (choice == 0)
					{
						System.exit(1);
					}
					if (choice == 1)
					{
						System.out.println("Not support in framework now.");
						continue;
					}
					
					InstalledPlugin p = table.get(choice);
					if (null == p.plugin.getAdminInterface())
					{
						System.out.println("Not support in plugin:"
								+ p.desc.name + " now.");
					}
					else
					{
						p.plugin.getAdminInterface().run();
					}
					continue;
				}
			}
			catch (Exception e)
			{
				//
			}
			System.err.println("Wrong input:" + s);
		}

	}

}
