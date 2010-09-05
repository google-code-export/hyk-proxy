/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: PluginManager.java 
 *
 * @author yinqiwen [ 2010-6-14 | 07:32:22 PM ]
 *
 */
package com.hyk.proxy.framework.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.appdata.AppData;
import com.hyk.proxy.framework.common.Constants;
import com.hyk.proxy.framework.launch.Upgrade;
import com.hyk.proxy.framework.management.ManageResource;
import com.hyk.proxy.framework.trace.Trace;
import com.hyk.util.classpath.JarClassLoader;
import com.hyk.util.classpath.JarClassLoaderPath;
import com.hyk.util.io.FileUtil;

/**
 *
 */
public class PluginManager implements ManageResource
{
	private Map<String, InstalledPlugin> plugins = new HashMap<String, InstalledPlugin>();
	private Map<String, RemoveMarkTask> removingTasks = new HashMap<String, PluginManager.RemoveMarkTask>();
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected static Properties pluginsStat = new Properties();
	private static PluginManager instance = new PluginManager();

	static enum ActiveState
	{
		ACTIVE, DEACTIIVE
	}

	private PluginManager()
	{
		try
		{
			pluginsStat.load(new FileInputStream(AppData.getUserPluginState()));
		}
		catch (IOException e)
		{
			logger.error("Can not load plugins state file.", e);
		}
	}

	public boolean isPluginInstalled(String name)
	{
		return plugins.containsKey(name);
	}

	private ActiveState getPersistentPluginActiveState(String name)
	{
		String value = pluginsStat.getProperty(name);
		if (null != value)
		{
			return Enum.valueOf(ActiveState.class, value);
		}
		return ActiveState.ACTIVE;
	}

	private void storePluginsActiveState(InstalledPlugin plugin,
	        ActiveState state)
	{
		try
		{
			pluginsStat.setProperty(plugin.desc.name, state.toString() + "");
			pluginsStat.store(
			        new FileOutputStream(AppData.getUserPluginState()), "");
		}
		catch (Exception e)
		{
			logger.error("Can not store plugins state file.", e);
		}
	}

	public static PluginManager getInstance()
	{
		return instance;
	}

	public static class InstalledPlugin
	{
		public PluginContext context;
		public JarClassLoader classloader;
		public Plugin plugin;
		public PluginDescription desc;
		public PluginState state;
		public boolean isGlobal;
	}

	public Collection<InstalledPlugin> getAllInstalledPlugins()
	{
		return plugins.values();
	}

	public InstalledPlugin getInstalledPlugin(String name)
	{
		return plugins.get(name);
	}

	protected static void retrieveJarFiles(String dir, List<String> jarFiles)
	{
		File dirfile = new File(dir);
		String[] list = dirfile.list();
		if (null != list)
		{
			for (String file : list)
			{
				String path = dir + Constants.FILE_SP + file;
				if (file.endsWith(".jar"))
				{
					jarFiles.add(path);
				}
				if (new File(path).isDirectory())
				{
					retrieveJarFiles(path, jarFiles);
				}
			}
		}

	}

	public PluginDescription getPluginDescription(String name)
	{
		InstalledPlugin plugin = plugins.get(name);
		if (null != plugin)
		{
			return plugin.desc;
		}
		return null;
	}

	protected String extractPluginZipFile(File zipFile)
	{
		try
		{
			List<String> beforeInstall = null;
			String[] afterInstall = null;
			if (FileUtil.canWrite(AppData.getPluginsHome()))
			{
				beforeInstall = Arrays.asList(AppData.getPluginsHome().list());
				FileUtil.unzip(zipFile, AppData.getPluginsHome(), false);
				afterInstall = AppData.getPluginsHome().list();
			}
			else
			{
				beforeInstall = Arrays.asList(AppData.getUserPluginsHome()
				        .list());
				FileUtil.unzip(zipFile, AppData.getUserPluginsHome(), false);
				afterInstall = AppData.getUserPluginsHome().list();
			}
			String dir = null;
			for (int i = 0; i < afterInstall.length; i++)
			{
				if (!beforeInstall.contains(afterInstall[i]))
				{
					dir = afterInstall[i];
					break;
				}
			}
			return dir;
		}
		catch (Exception e)
		{
			logger.error("Failed to load plugin:" + zipFile.getName(), e);
			return null;
		}
		finally
		{
			zipFile.delete();
		}
	}

	public InstalledPlugin loadPlugin(File zipFile, Trace trace)
	{
		try
		{
			String dir = extractPluginZipFile(zipFile);
			if (null == dir)
			{
				logger.error("No created dir found for zip file:"
				        + zipFile.getName());
				return null;
			}
			InstalledPlugin resolve = resolvePlugin(dir,
			        FileUtil.canWrite(AppData.getPluginsHome()), trace);
			return loadPlugin(resolve, trace);
		}
		catch (Exception e)
		{
			logger.error("Failed to load plugin:" + zipFile.getName(), e);
			return null;
		}
	}

	private InstalledPlugin resolvePlugin(String dir, boolean isGlobal,
	        Trace trace)
	{
		File home = null;
		if (isGlobal)
		{
			home = new File(AppData.getPluginsHome().getAbsolutePath()
			        + Constants.FILE_SP + dir);
		}
		else
		{
			home = new File(AppData.getUserPluginsHome().getAbsolutePath()
			        + Constants.FILE_SP + dir);
		}
		String[] homedir = new String[] { home.getAbsolutePath(),
		        home.getAbsolutePath() + Constants.FILE_SP + "etc" };
		String libdir = home.getAbsolutePath() + Constants.FILE_SP + "lib";
		List<String> jarFiles = new ArrayList<String>();
		retrieveJarFiles(libdir, jarFiles);
		String[] jarentries = new String[jarFiles.size()];
		jarFiles.toArray(jarentries);
		JarClassLoader loader;
		String pluginName = null;
		PluginContext pluginContext = new PluginContext();
		try
		{
			JarClassLoaderPath classPath = new JarClassLoaderPath(jarentries,
			        homedir);
			loader = new JarClassLoader(PluginManager.class.getClassLoader(),
			        classPath);
			JAXBContext context = JAXBContext
			        .newInstance(PluginDescription.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			URL pluginResource = loader.getResource("/"
			        + Constants.PLUGIN_DESC_FILE);
			PluginDescription desc = (PluginDescription) unmarshaller
			        .unmarshal(pluginResource);
			pluginName = desc.name;
			if (plugins.containsKey(pluginName))
			{
				logger.error("Can not resolve plugin with duplicate name:"
				        + pluginName);
				return null;
			}
			pluginContext.setHome(home);
			pluginContext.setName(desc.name);
			pluginContext.setVersion(desc.version);
			InstalledPlugin resolve = new InstalledPlugin();
			resolve.desc = desc;
			resolve.classloader = loader;
			resolve.context = pluginContext;
			resolve.state = PluginState.RESOLVED;
			resolve.isGlobal = isGlobal;
			plugins.put(pluginName, resolve);
			return resolve;
		}
		catch (Exception e)
		{
			logger.error("Failed to resolve plugin in dir:" + dir, e);
			trace.error("Resolve plugin:" + pluginName + "  ...   Failed");
		}
		return null;
	}

	protected InstalledPlugin loadPlugin(InstalledPlugin resolve, Trace trace)
	{
		String pluginName = resolve.desc.name;
		try
		{
			for (String depend : resolve.desc.depends)
			{
				if (null != depend && !depend.trim().isEmpty())
				{
					InstalledPlugin dependPlugin = plugins.get(depend);
					if (null != dependPlugin)
					{
						resolve.classloader
						        .addDependClassLoader(dependPlugin.classloader);
					}
					else
					{
						trace.error("No depend plugin:" + depend + " found.");
					}
				}
			}
			Class clazz = resolve.classloader
			        .loadClass(resolve.desc.entryClass);
			Plugin plugin = (Plugin) clazz.newInstance();
			plugin.onLoad(resolve.context);
			resolve.state = PluginState.LOADED;
			resolve.plugin = plugin;
			if (removingTasks.containsKey(pluginName))
			{
				removingTasks.remove(pluginName).disable();
			}
			trace.notice("Load plugin:" + pluginName + " ...   Success");
			return resolve;
		}
		catch (Exception e)
		{
			logger.error("Failed to load plugin.", e);
			trace.error("Load plugin:" + pluginName + " ...   Failed");
			plugins.remove(pluginName);
		}

		return null;

	}

	public void loadPlugins(Trace trace)
	{
		if (AppData.getPluginsHome().exists()
		        && AppData.getPluginsHome().isDirectory())
		{
			String[] dirs = AppData.getPluginsHome().list();
			for (String fname : dirs)
			{
				if (null == fname || fname.trim().isEmpty())
				{
					continue;
				}
				File file = new File(AppData.getPluginsHome().getAbsolutePath()
				        + Constants.FILE_SP + fname);
				if (file.isDirectory())
				{
					resolvePlugin(fname, true, trace);
				}
				else if (fname.endsWith(".zip"))
				{
					String dir = extractPluginZipFile(file);
					if (null != dir)
					{
						resolvePlugin(dir,
						        FileUtil.canWrite(AppData.getPluginsHome()),
						        trace);
					}
				}
			}
		}
		String[] dirs = AppData.getUserPluginsHome().list();
		for (String dir : dirs)
		{
			if (null == dir || dir.trim().isEmpty())
			{
				continue;
			}
			if (new File(AppData.getUserPluginsHome().getAbsolutePath()
			        + Constants.FILE_SP + dir).isDirectory())
			{
				resolvePlugin(dir, false, trace);
			}
		}
		for (InstalledPlugin resolve : plugins.values())
		{
			loadPlugin(resolve, trace);
		}
	}

	public void activatePlugin(InstalledPlugin plugin, Trace trace)
	{
		if (plugin.state == PluginState.ACTIVATED)
		{
			return;
		}
		try
		{
			plugin.plugin.onActive(plugin.context);
			plugin.state = PluginState.ACTIVATED;
			storePluginsActiveState(plugin, ActiveState.ACTIVE);
			trace.notice("Active plugin:" + plugin.desc.name + " ...   Success");
		}
		catch (Exception e)
		{
			logger.error("Failed to active plugin:" + plugin.desc.name, e);
			trace.error("Active plugin:" + plugin.desc.name + " ...   Failed");
		}
	}

	public void activatePlugins(Trace trace)
	{
		Collection<InstalledPlugin> c = plugins.values();
		for (InstalledPlugin plugin : c)
		{
			if (getPersistentPluginActiveState(plugin.desc.name).equals(
			        ActiveState.ACTIVE))
			{
				activatePlugin(plugin, trace);
			}
		}
	}

	public void deactivePlugin(String name, Trace trace) throws Exception
	{
		InstalledPlugin plugin = plugins.get(name);
		if (null != plugin && plugin.state.equals(PluginState.ACTIVATED))
		{
			plugin.plugin.onDeactive(plugin.context);
			plugin.state = PluginState.LOADED;
			storePluginsActiveState(plugin, ActiveState.DEACTIIVE);
			trace.notice("Deactive plugin:" + name + " ...   Success");
		}
	}

	public void unloadPlugin(String name, Trace trace) throws Exception
	{
		InstalledPlugin plugin = plugins.get(name);
		if (null != plugin && plugin.state.equals(PluginState.LOADED))
		{
			plugin.plugin.onUnload(plugin.context);
			plugin.state = PluginState.RESOLVED;
			plugins.remove(name);
			FileUtil.deleteFile(plugin.context.getHome());
			storePluginsActiveState(plugin, ActiveState.ACTIVE);
			plugins.remove(name);
			// visitedPluginDirs.remove(plugin.dir.getName());
			RemoveMarkTask task = new RemoveMarkTask(plugin);
			removingTasks.put(plugin.desc.name, task);
			Runtime.getRuntime().addShutdownHook(new Thread(task));
			trace.notice("Unload plugin:" + name + " ...   Success");
		}
	}

	@Override
	public String handleManagementCommand(String cmd)
	{
		return null;
	}

	@Override
	public String getName()
	{
		return Constants.PLUGIN_MANAGER_NAME;
	}

	class RemoveMarkTask implements Runnable
	{
		public RemoveMarkTask(InstalledPlugin removingPlugin)
		{
			this.removingPlugin = removingPlugin;
		}

		private InstalledPlugin removingPlugin;
		private boolean enable = true;

		public void disable()
		{
			enable = false;
		}

		@Override
		public void run()
		{
			try
			{
				if (enable)
				{
					String prefix = "";
					if (!removingPlugin.isGlobal)
					{
						prefix = ".";
					}
					Upgrade.appendRemoveDetail(prefix + "plugins"
					        + Constants.FILE_SP
					        + removingPlugin.context.getHome().getName());
					Upgrade.flushUpgradeDetails();
				}
			}
			catch (IOException e)
			{
				logger.error("Failed to write remove info:", e);
			}

		}

	}
}
