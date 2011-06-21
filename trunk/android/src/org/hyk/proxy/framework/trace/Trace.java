/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Trace.java 
 *
 * @author yinqiwen [ 2010-8-13 | 11:07:07 AM ]
 *
 */
package org.hyk.proxy.framework.trace;

/**
 *
 */
public interface Trace
{
	public void notice(String msg);
	public void info(String msg);
	public void error(String msg);
}
