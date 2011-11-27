/**
 * This file is part of the hyk-proxy-core project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BasicConfiguration.java 
 *
 * @author yinqiwen [ 2011-11-27 | ÏÂÎç09:10:32 ]
 *
 */
package org.hyk.proxy.core.config;

import java.io.InputStream;
import java.util.Properties;

import org.arch.config.IniProperties;
import org.hyk.proxy.core.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class BasicConfiguration
{
	protected static Logger logger = LoggerFactory
	        .getLogger(BasicConfiguration.class);

	private IniProperties props = null;
	

	protected BasicConfiguration()
	{
		init();
	}


	protected abstract String getTagName();
	private void init()
	{
		InputStream is = BasicConfiguration.class
		        .getResourceAsStream("/hyk-proxy.conf" + Constants.CONF_FILE);
		props = new IniProperties();
		try
		{
			props.load(is);
			doInit(props.getProperties(getTagName()));
		}
		catch (Exception e)
		{
			logger.error("Failed to init configuration.", e);
		}
	}
	
	protected abstract void doInit(Properties props);

	public void reLoad()
	{
		init();
	}
}
