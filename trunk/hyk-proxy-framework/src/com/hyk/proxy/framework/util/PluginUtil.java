/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: PluginUtil.java 
 *
 * @author yinqiwen [ 2010-8-29 | 04:09:18 PM ]
 *
 */
package com.hyk.proxy.framework.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.common.Constants;

/**
 *
 */
public class PluginUtil
{
	protected static Logger logger = LoggerFactory.getLogger(PluginUtil.class);

	public static InputStream peekPluginDescFile(File zipFile)
	{
		try
		{
			ZipFile zip = new ZipFile(zipFile);
			Enumeration emu = zip.entries();
			byte data[] = new byte[4096];
			while (emu.hasMoreElements())
			{
				ZipEntry entry = (ZipEntry) emu.nextElement();
				if (entry.isDirectory()
				        || !entry.getName()
				                .endsWith(Constants.PLUGIN_DESC_FILE))
				{
					continue;
				}
				BufferedInputStream bis = new BufferedInputStream(
				        zip.getInputStream(entry));

				zip.close();
				return bis;
			}
			zip.close();
		}
		catch (Exception e)
		{
			logger.error("Failed to get plugin desc file in plugin zip file.",
			        e);
		}
		return null;
	}
}
