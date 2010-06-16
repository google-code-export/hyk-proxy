/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: PluginManager.java 
 *
 * @author yinqiwen [ 2010-6-14 | 07:32:22 PM ]
 *
 */
package com.hyk.proxy.client.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.hyk.util.classpath.JarClassLoader;

/**
 *
 */
public class PluginManager
{
	private Map<String, PluginLifeCycle>	plugins	= new HashMap<String, PluginLifeCycle>();

	private static PluginManager instance = new PluginManager();
	
	private PluginManager(){}
	
	public static PluginManager getInstance()
	{
		return instance;
	}
	
	static class PluginLifeCycle
	{
		public PluginLifeCycle(Plugin plugin, PluginDescription desc, PluginState state)
		{
			this.plugin = plugin;
			this.desc = desc;
			this.state = state;
		}

		Plugin				plugin;
		PluginDescription	desc;
		PluginState			state;
	}

	protected static void retrieveJarFiles(String dir, List<String> jarFiles)
	{
		File dirfile = new File(dir);
		String[] list = dirfile.list();
		if(null != list)
		{
			for(String file : list)
			{
				String path = dir + System.getProperty("file.separator") + file;
				if(file.endsWith(".jar"))
				{
					jarFiles.add(path);
				}
				if(new File(path).isDirectory())
				{
					retrieveJarFiles(path, jarFiles);
				}
			}
		}
		
	}

	protected Plugin loadPlugin(File pluginDir, String dir) throws Exception
	{
		File home = new File(pluginDir.getAbsolutePath() + System.getProperty("file.separator") + dir);
		String[] homedir = new String[] {home.getAbsolutePath()};
		// String descfile = home.getAbsolutePath() +
		// System.getProperty("file.separator") + "plugin.xml";
		String libdir = home.getAbsolutePath() + System.getProperty("file.separator") + "lib";
		List<String> jarFiles = new ArrayList<String>();
		retrieveJarFiles(libdir, jarFiles);
		String[] jarentries = new String[jarFiles.size()];
		jarFiles.toArray(jarentries);
		JarClassLoader loader = new JarClassLoader(PluginManager.class.getClassLoader(), jarentries, homedir);
		JAXBContext context = JAXBContext.newInstance(PluginDescription.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		PluginDescription desc = (PluginDescription)unmarshaller.unmarshal(loader.getResource("/plugin.xml"));
		Class clazz = loader.loadClass(desc.entryClass);
		Plugin plugin = (Plugin)clazz.newInstance();
		plugin.onLoad();
		plugins.put(dir, new PluginLifeCycle(plugin, desc, PluginState.LOADED));
		return plugin;
	}

	public void loadPlugins() throws Exception
	{
		String home = System.getProperty("HYK_PROXY_CLIENT_HOME");
		File pluginDir = new File(home + System.getProperty("file.separator") + "plugin");
		if(pluginDir.exists() && pluginDir.isDirectory())
		{
			String[] dirs = pluginDir.list();
			for(String dir : dirs)
			{
				if(new File(pluginDir.getAbsolutePath() + System.getProperty("file.separator") + dir).isDirectory())
				{
					loadPlugin(pluginDir, dir);
				}
			}
		}
	}

	private void activatePlugin(PluginLifeCycle plugin) throws Exception
	{
		plugin.plugin.onActive();
		plugin.state = PluginState.ACTIVATED;
	}

	public void activatePlugins() throws Exception
	{
		Collection<PluginLifeCycle> c = plugins.values();
		for(PluginLifeCycle plugin : c)
		{
			if(plugin.state.equals(PluginState.ACTIVATED))
			{
				continue;
			}
			if(null != plugin.desc.depends)
			{
				for(String depend : plugin.desc.depends)
				{
					PluginLifeCycle other = plugins.get(depend);
					if(null != other)
					{
						activatePlugin(other);
					}
				}
			}
			activatePlugin(plugin);
		}
	}

	public static void main(String[] args) throws Exception
	{
		PluginManager m = new PluginManager();
		File pluginDir = new File("plugin");
		m.loadPlugin(pluginDir, "spac");
	}
}
