/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SetCookieHeaderListValue.java 
 *
 * @author qiying.wang [ Feb 2, 2010 | 4:49:54 PM ]
 *
 */
package com.hyk.proxy.gae.common.http;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * 
 */
public class SetCookieHeaderListValue implements HttpHeaderValue
{
	private String value;
	private List<SetCookieHeaderValue> setCookies = null;
	public SetCookieHeaderListValue(String value)
	{
		this.value = value;
	}
	
	public List<SetCookieHeaderValue> getSetCookies()
	{
		if(null == setCookies)
		{
			setCookies = new LinkedList<SetCookieHeaderValue>();
			int cursor = 0;
			int limit = value.length();
			
		}
		
		return setCookies;
	}
}
