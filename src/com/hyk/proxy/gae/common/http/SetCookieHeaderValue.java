/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SetCookieHeaderValue.java 
 *
 * @author qiying.wang [ Feb 2, 2010 | 4:49:21 PM ]
 *
 */
package com.hyk.proxy.gae.common.http;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Set-Cookie: <name>=<value>[; <name>=<value>]... [; expires=<date>][;domain=<domain_name>] [; path=<some_path>][; secure][; httponly]
 */
public class SetCookieHeaderValue implements HttpHeaderValue
{
	private String value;
	
	public SetCookieHeaderValue(String value)
	{
		this.value = value;
	}

	public static List<SetCookieHeaderValue> parse(String value)
	{
		LinkedList<String> headerValues = new LinkedList<String>();
		String[] temp = value.split(",");
		for(String v:temp)
		{
			if(v.indexOf("=") == -1
					|| (v.indexOf("=") > v.indexOf(";")))
			{
				headerValues.add(headerValues.removeLast() + "," + v);
			}
			else
			{
				headerValues.add(v);
			}
		}
		List<SetCookieHeaderValue> hvs = new LinkedList<SetCookieHeaderValue>();
		for(String v:headerValues)
		{
			hvs.add(new SetCookieHeaderValue(v.trim()));
		}
		return hvs;
	}
	
	public String toString()
	{
		return value;
	}
}
