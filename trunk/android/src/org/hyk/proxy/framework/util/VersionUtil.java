/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: VersionUtil.java 
 *
 * @author yinqiwen [ 2010-8-21 | 03:03:51 PM ]
 *
 */
package org.hyk.proxy.framework.util;

/**
 *
 */
public class VersionUtil
{
	private static int compare(String v1, String v2)
	{
		String[] v1_int = v1.split(".");
		String[] v2_int = v2.split(".");
		int compareLen = Math.min(v1_int.length, v2_int.length);
		for (int i = 0; i < compareLen; i++)
        {
			int a = Integer.parseInt(v1_int[i]);
			int b = Integer.parseInt(v2_int[i]);
	        if(a == b)
	        {
	        	continue;
	        }
	        return a - b;
        }
		return v1_int.length - v2_int.length;
	}

	public static boolean match(String version, String from, String to)
	{
		boolean matchFrom = true;
		if (null != from)
		{
			matchFrom = (compare(version, from) >= 0);
		}
		boolean matchTo = true;
		if (null != to)
		{
			matchTo = (compare(version, to) <= 0);
		}
		return matchFrom && matchTo;
	}
}
