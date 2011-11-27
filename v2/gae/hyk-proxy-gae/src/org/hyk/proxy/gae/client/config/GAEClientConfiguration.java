/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: GAEClientConfiguration.java 
 *
 * @author yinqiwen [ 2011-11-27 | ÏÂÎç09:42:39 ]
 *
 */
package org.hyk.proxy.gae.client.config;

import java.util.Properties;

import org.hyk.proxy.core.config.BasicConfiguration;

/**
 *
 */
public class GAEClientConfiguration extends BasicConfiguration
{
	private CompressorType compressor = CompressorType.SNAPPY;
	
	@Override
	protected String getTagName()
	{
		return "GAE";
	}

	@Override
	protected void doInit(Properties props)
	{
		String typestr = props.getProperty("Compressor");
		typestr = typestr.toUpperCase();
		compressor = CompressorType.valueOf(typestr);
	}
}
