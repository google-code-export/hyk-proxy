/**
 * This file is part of the hyk-proxy-android project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: StatusHelper.java 
 *
 * @author yinqiwen [ 2011-6-25 | ÏÂÎç08:59:46 ]
 *
 */
package org.hyk.proxy.android.helper;

import java.util.Date;

import android.widget.TextView;

/**
 *
 */
public class StatusHelper
{
	private static TextView statusView = null;
	
	public static void SetStatusView(TextView view)
	{
		if(null != view)
		{
			statusView = view;
		}
	}
	
	public static void log(String msg)
	{
		if(null == statusView)
		{
			return;
		}
		Date now = new Date(System.currentTimeMillis());
		StringBuilder buffer = new StringBuilder();
		buffer.append("[").append(now.getHours()).append(":").append(now.getMinutes()).append(":").append(now.getSeconds()).append("]");
		buffer.append(msg).append("\n");
		statusView.append(buffer);
	}
	
	public static void log(String msg, TextView view)
	{
		SetStatusView(view);
		log(msg);
	}
}
