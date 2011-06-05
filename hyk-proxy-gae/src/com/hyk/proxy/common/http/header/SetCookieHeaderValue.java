/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SetCookieHeaderValue.java 
 *
 * @author yinqiwen [ Feb 2, 2010 | 4:49:21 PM ]
 *
 */
package com.hyk.proxy.common.http.header;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set-Cookie: <name>=<value>[; <name>=<value>]... [;
 * expires=<date>][;domain=<domain_name>] [; path=<some_path>][; secure][;
 * httponly]
 */
public class SetCookieHeaderValue implements HttpHeaderValue
{
	protected static Logger logger = LoggerFactory
	        .getLogger(SetCookieHeaderValue.class);

	private String value;

	public SetCookieHeaderValue(String value)
	{
		this.value = value;
	}

	public static List<SetCookieHeaderValue> parse(String value)
	{
		List<SetCookieHeaderValue> hvs = new LinkedList<SetCookieHeaderValue>();
		try
		{
			LinkedList<String> headerValues = new LinkedList<String>();
			String[] temp = value.split(",");
			if (temp.length > 1)
			{
				for (String v : temp)
				{
					if ((v.indexOf("=") == -1 || (v.indexOf("=") > v
					        .indexOf(";"))) && !headerValues.isEmpty())
					{
						headerValues.add(headerValues.removeLast() + "," + v);
					}
					else
					{
						headerValues.add(v);
					}
				}
			}
			else
			{
				headerValues.add(value.trim());
			}

			for (String v : headerValues)
			{
				hvs.add(new SetCookieHeaderValue(v.trim()));
			}
			return hvs;
		}
		catch (Exception e)
		{
			logger.error("Failed to parse SetCookie header:" + value, e);
			return hvs;
		}

	}

	public String toString()
	{
		return value;
	}

//	public static void main(String[] args)
//	{
//		SetCookieHeaderValue p = new SetCookieHeaderValue("");
//		List<SetCookieHeaderValue> v = p.parse("t7asq_4ad6_sid=2VIvQ2; expires=Sun, 29-May-2011 03:13:41 GMT; path=/; domain=.discuz.net, t7asq_4ad6_lastact=1306552421%09member.php%09logging; expires=Sun, 29-May-2011 03:13:41 GMT; path=/; domain=.discuz.net");
//		System.out.println(v.get(1));
//	}
}
