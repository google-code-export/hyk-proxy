package org.arch.classloader;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ArchClassLoader extends ClassLoader
{
	private static final String FILE_SP = System.getProperty("file.separator");
	
	private List<ClassLoader> dependLoaders = new ArrayList<ClassLoader>();
	private JarClassLoaderPath classPath;
	
	public ArchClassLoader(ClassLoader parent, JarClassLoaderPath path)
	{
		super(parent == null ? ClassLoader.getSystemClassLoader() : parent);
		this.classPath = path;
	}
	
	public void addDependClassLoader(ClassLoader loader)
	{
		dependLoaders.add(loader);
	}

	protected SearchResult getEntry(String name) throws IOException
	{
		for (JarFile jar : classPath.jars)
		{
			JarEntry entry = jar.getJarEntry(name);
			if (null == entry) continue;
			URL url = new URL("jar:file:" + jar.getName() + "!/" + entry);
			return new SearchResult(url, (int) entry.getSize());
		}

		for (String path : classPath.dirs)
		{
			if (!path.endsWith(FILE_SP))
			{
				path = path + FILE_SP;
			}
			File f = new File(path + name);

			if (f.exists())
			{
				return new SearchResult(f.toURI().toURL(), (int) f.length());
			}
		}

		return null;
	}

	protected SearchResult getClassEntry(String name) throws IOException
	{
		name = name.replace(".", "/");
		name = name + ".class";

		return getEntry(name);
	}

	protected Class loadClassEntry(SearchResult classEntry, String name)
	        throws IOException
	{
		URL url = classEntry.url;
		DataInputStream is = new DataInputStream(url.openStream());
		byte[] buffer = new byte[classEntry.len];
		is.readFully(buffer);
		return defineClass(name, buffer, 0, buffer.length);
	}

	protected Class<?> findClassInDepends(String name)
	{
		for(ClassLoader loader:dependLoaders)
		{
			try
            {
	            return loader.loadClass(name);
            }
            catch (ClassNotFoundException e)
            {
            	//just continue
	            continue;
            }
		}
		return null;
	}
	
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		try
		{
			return getParent().loadClass(name);
		}
		catch (Throwable e)
		{
			try
			{
				Class<?> ret = findClassInDepends(name);
				if(null != ret)
				{
					return ret;
				}
				SearchResult classEntry = getClassEntry(name);
				if (null != classEntry)
				{
					return loadClassEntry(classEntry, name);
				}

			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		throw new ClassNotFoundException(name);
	}

	protected URL findResource(String name)
	{
		try
		{
			if (name.startsWith("/"))
			{
				name = name.substring(1, name.length());
			}
			SearchResult result = getEntry(name);
			
			return null != result ?result.url:null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	static class SearchResult
	{
		URL url;
		int len;

		public SearchResult(URL url, int len)
		{
			this.url = url;
			this.len = len;
		}
	}
}