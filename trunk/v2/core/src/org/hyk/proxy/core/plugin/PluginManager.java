/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: PluginManager.java 
 *
 * @author yinqiwen [ 2010-6-14 | 07:32:22 PM ]
 *
 */
package org.hyk.proxy.core.plugin;

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

import org.arch.classloader.ArchClassLoader;
import org.arch.util.FileHelper;
import org.hyk.proxy.core.common.AppData;
import org.hyk.proxy.core.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 */
public class PluginManager
{
	private Map<String, InstalledPlugin> plugins = new HashMap<String, InstalledPlugin>();
	//private Map<String, RemoveMarkTask> removingTasks = new HashMap<String, PluginManager.RemoveMarkTask>();
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected static Properties pluginsStat = new Properties();
	private static PluginManager instance = new PluginManager();

	static enum ActiveState
	{
		ACTIVE, DEACTIIVE
	}

	private PluginManager()
	{
		
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
		public ArchClassLoader classloader;
		public Plugin plugin;
		public PluginDescription desc;
		public PluginState state;
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
			if (FileHelper.canWrite(AppData.getPluginsHome()))
			{
				beforeInstall = Arrays.asList(AppData.getPluginsHome().list());
				boolean replace = false;
				FileHelper.unzip(zipFile, AppData.getPluginsHome(), replace);
				afterInstall = AppData.getPluginsHome().list();
			}
			else
			{
				beforeInstall = Arrays.asList(AppData.getUserPluginsHome()
				        .list());
				FileHelper.unzip(zipFile, AppData.getUserPluginsHome(), false);
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

	public InstalledPlugin loadPlugin(File zipFile)
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
					FileHelper.canWrite(AppData.getPluginsHome()));
			return loadPlugin(resolve);
		}
		catch (Exception e)
		{
			logger.error("Failed to load plugin:" + zipFile.getName(), e);
			return null;
		}
	}

	private InstalledPlugin resolvePlugin(String dir, boolean isGlobal)
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
			plugins.put(pluginName, resolve);
			return resolve;
		}
		catch (Exception e)
		{
			logger.error("Failed to resolve plugin in dir:" + dir, e);
			//trace.error("Resolve plugin:" + pluginName + "  ...   Failed");
		}
		return null;
	}

	protected InstalledPlugin loadPlugin(InstalledPlugin resolve)
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
						logger.error("No depend plugin:" + depend + " found.");
					}
				}
			}
			Class clazz = resolve.classloader
			        .loadClass(resolve.desc.entryClass);
			Plugin plugin = (Plugin) clazz.newInstance();
			plugin.onLoad(resolve.context);
			resolve.state = PluginState.LOADED;
			resolve.plugin = plugin;

			logger.info("Load plugin:" + pluginName + " ...   Success");
			return resolve;
		}
		catch (Exception e)
		{
			logger.error("Failed to load plugin.", e);
			//trace.error("Load plugin:" + pluginName + " ...   Failed");
			plugins.remove(pluginName);
		}

		return null;

	}

	public void loadPlugins()
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
					try
					{
						resolvePlugin(fname, true);
					}
					catch (Exception e)
					{
						logger.error("Failed to resolve plugin in dir :" + fname, e);
					}

				}
				else if (fname.endsWith(".zip"))
				{
					String dir = extractPluginZipFile(file);
					if (null != dir)
					{
						try
                        {
							resolvePlugin(dir,
							        FileHelper.canWrite(AppData.getPluginsHome()));
                        }
                        catch (Exception e)
                        {
                        	logger.error("Failed to resolve plugin for zip file:" + fname, e);
                        }	
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
				resolvePlugin(dir, false);
			}
		}
		for (InstalledPlugin resolve : plugins.values())
		{
			loadPlugin(resolve);
		}
	}

	public void activatePlugin(InstalledPlugin plugin)
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
			logger.info("Active plugin:" + plugin.desc.name + " ...   Success");
		}
		catch (Exception e)
		{
			logger.error("Failed to active plugin:" + plugin.desc.name, e);
			//logger.error("Active plugin:" + plugin.desc.name + " ...   Failed");
		}
	}

	public void activatePlugins()
	{
		Collection<InstalledPlugin> c = plugins.values();
		for (InstalledPlugin plugin : c)
		{
			if (getPersistentPluginActiveState(plugin.desc.name).equals(
			        ActiveState.ACTIVE))
			{
				activatePlugin(plugin);
			}
		}
	}

	public void deactivePlugin(String name) throws Exception
	{
		InstalledPlugin plugin = plugins.get(name);
		if (null != plugin && plugin.state.equals(PluginState.ACTIVATED))
		{
			plugin.plugin.onDeactive(plugin.context);
			plugin.state = PluginState.LOADED;
			storePluginsActiveState(plugin, ActiveState.DEACTIIVE);
			logger.info("Deactive plugin:" + name + " ...   Success");
		}
	}

	public void unloadPlugin(String name) throws Exception
	{
		InstalledPlugin plugin = plugins.get(name);
		if (null != plugin && plugin.state.equals(PluginState.LOADED))
		{
			plugin.plugin.onUnload(plugin.context);
			plugin.state = PluginState.RESOLVED;
			plugins.remove(name);
			FileHelper.deleteFile(plugin.context.getHome());
			storePluginsActiveState(plugin, ActiveState.ACTIVE);
			plugins.remove(name);
			// visitedPluginDirs.remove(plugin.dir.getName());
			//RemoveMarkTask task = new RemoveMarkTask(plugin);
			//removingTasks.put(plugin.desc.name, task);
			//Runtime.getRuntime().addShutdownHook(new Thread(task));
			//trace.notice("Unload plugin:" + name + " ...   Success");
			logger.info("Unload plugin:" + name + " ...   Success");
		}
	}

}
