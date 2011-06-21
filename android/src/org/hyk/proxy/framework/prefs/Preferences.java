/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Preferences.java 
 *
 * @author yinqiwen [ 2010-8-15 | 12:56:32 PM ]
 *
 */
package org.hyk.proxy.framework.prefs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.hyk.proxy.framework.appdata.AppData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Preferences
{
	private static Logger logger = LoggerFactory.getLogger(Preferences.class);
	
	private static Properties props = new Properties();
	
	public static void init()
	{
		try
        {
			props.load(new FileInputStream(AppData.getUserPrefernceFile()));
        }
        catch (Exception e)
        {
        	logger.error("Failed to init prefernce.", e);
        }
	}
	
	public static String getPreferenceValue(String name)
	{
		return props.getProperty(name);
	}
	
	public static void setPrefernceValue(String name, String value)
	{
		props.setProperty(name, value);
		try
        {
			FileOutputStream fos = new FileOutputStream(AppData.getUserPrefernceFile());
	        props.store(fos, "");
	        fos.close();
        }
        catch (Exception e)
        {
	        logger.error("Failed to store preference value.", e);
        }
	}
}
