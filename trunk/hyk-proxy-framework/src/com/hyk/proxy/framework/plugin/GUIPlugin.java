/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: GUIPlugin.java 
 *
 * @author yinqiwen [ 2010-8-16 | 08:31:53 AM ]
 *
 */
package com.hyk.proxy.framework.plugin;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 */
public interface GUIPlugin extends Plugin
{
	public ImageIcon getIcon();
	public JPanel getConfigPanel();
}
