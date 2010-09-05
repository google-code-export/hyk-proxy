/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: StartClient.java 
 *
 * @author yinqiwen [ 2010-1-31 | 04:33:16 PM ]
 *
 */
package com.hyk.proxy.framework.shell.tui;

import com.hyk.proxy.framework.Framework;
import com.hyk.proxy.framework.shell.tui.TUITrace;

/**
 *
 */
public class StartProxyFramework
{
	public static void main(String[] args)
	{
		Framework fm = new Framework(new TUITrace());
		if (!fm.start())
		{
			System.console().printf("Press <Enter> to exit...");
			System.console().readLine();
			System.exit(1);
		}
	}
}
