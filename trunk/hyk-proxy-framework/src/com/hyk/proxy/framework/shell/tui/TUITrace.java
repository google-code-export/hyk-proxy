/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: TUITrace.java 
 *
 * @author yinqiwen [ 2010-8-13 | 11:12:26 AM ]
 *
 */
package com.hyk.proxy.framework.shell.tui;

import com.hyk.proxy.framework.trace.Trace;

/**
 *
 */
public class TUITrace implements Trace
{
	@Override
	public void info(String msg)
	{
		System.out.println(msg);
	}

	@Override
    public void error(String msg)
    {
		System.err.println(msg);
    }

	@Override
    public void notice(String msg)
    {
		System.out.println(msg);
    }
}
