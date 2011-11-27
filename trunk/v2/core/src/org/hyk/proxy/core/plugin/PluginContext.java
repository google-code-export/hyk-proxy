/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: PluginContext.java 
 *
 * @author yinqiwen [ 2010-8-25 | 08:26:57 PM]
 *
 */
package org.hyk.proxy.core.plugin;

import java.io.File;

/**
 *
 */
public class PluginContext
{
	private File home;
	private String name;
	private String version;

	public String getVersion()
    {
    	return version;
    }

	public void setVersion(String version)
    {
    	this.version = version;
    }

	public String getName()
    {
    	return name;
    }

	public void setName(String name)
    {
    	this.name = name;
    }

	public File getHome()
    {
    	return home;
    }

	public void setHome(File home)
    {
    	this.home = home;
    }
	
}
