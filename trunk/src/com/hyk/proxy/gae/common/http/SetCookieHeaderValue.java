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
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Set-Cookie: <name>=<value>[; <name>=<value>]... [; expires=<date>][;domain=<domain_name>] [; path=<some_path>][; secure][; httponly]
 */
public class SetCookieHeaderValue implements HttpHeaderValue
{
	private Properties props;
	private String expires;
	private String domain;
	private String path;
	private boolean isSecure;
	private boolean isHttpOnly;
	
	public static SetCookieHeaderValue parse(CharBuffer value)
	{
		return null;
	}
	
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		Set<Entry<Object, Object>> entries = props.entrySet();
		boolean isFirst = true;
		for(Entry<Object, Object> entry:entries)
		{
			if(isFirst)
			{
				isFirst = false;
			}
			else
			{
				buffer.append(";");
			}
			buffer.append(entry.getKey()).append("=").append(entry.getValue());
		}
		if(null != expires)
		{
			buffer.append(";expires").append(expires);
		}
		if(null != domain)
		{
			buffer.append(";domain").append(domain);
		}
		if(null != path)
		{
			buffer.append(";path").append(path);
		}
		if(isSecure)
		{
			buffer.append(";secure");
		}
		if(isHttpOnly)
		{
			buffer.append(";httponly");
		}
		
		return buffer.toString();
	}
}
