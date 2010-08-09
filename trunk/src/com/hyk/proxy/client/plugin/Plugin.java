/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Plugin.java 
 *
 * @author yinqiwen [ 2010-6-14 | 07:30:14 PM ]
 *
 */
package com.hyk.proxy.client.plugin;

import java.util.Properties;

/**
 *
 */
public interface Plugin
{
	public void onLoad() throws Exception;
	public void onActive() throws Exception;
	public void onUnload() throws Exception;
	public void onDeactive() throws Exception;
	
}
