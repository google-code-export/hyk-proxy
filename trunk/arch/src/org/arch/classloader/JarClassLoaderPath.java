/**
 * This file is part of the hyk-util project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: JarClassLoaderPath.java 
 *
 * @author yinqiwen [ 2010-8-26 | 09:13:28 PM]
 *
 */
package org.arch.classloader;

import java.io.IOException;
import java.util.jar.JarFile;

/**
 *
 */
class JarClassLoaderPath
{
	public JarClassLoaderPath(String[] jarpaths, String[] dirs) throws IOException
    {
	    this.dirs = dirs;
	    if(null != jarpaths)
	    {
	    	jars = new JarFile[jarpaths.length];
	    	for(int i = 0; i < jarpaths.length; i++)
	    	{
	    		jars[i] = new JarFile(jarpaths[i], true);
	    	}
	    }
	    else
	    {
	    	jars = new JarFile[0];
	    }
	    
	    if(null == dirs)
	    {
	    	this.dirs = new String[0];
	    }
    }
	String[] dirs;
	JarFile[] jars;
}
