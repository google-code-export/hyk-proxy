/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Launch.java 
 *
 * @author yinqiwen [ 2010-8-19 | 01:52:52 PM ]
 *
 */
package com.hyk.proxy.framework.launch;

import com.hyk.proxy.framework.admin.Admin;
import com.hyk.proxy.framework.shell.gui.MainFrame;
import com.hyk.proxy.framework.shell.tui.StartProxyFramework;

/**
 *
 */
public class Launcher
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		boolean ret = Upgrade.upgrade();
		if(args.length == 1)
		{
			if(args[0].equals("tui"))
			{
				StartProxyFramework.main(null);
			}
			else if(args[0].equals("gui"))
			{
				MainFrame.main(null);
			}
			else if(args[0].equals("admin"))
			{
				Admin.main(null);
			}
		}

	}

}
