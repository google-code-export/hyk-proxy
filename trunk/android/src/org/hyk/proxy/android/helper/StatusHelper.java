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

import android.os.Handler;
import android.widget.TextView;

/**
 *
 */
public class StatusHelper
{
	private TextView statusView = null;
	private Handler handler = null;
	
	public StatusHelper(TextView view, Handler h)
	{
		statusView = view;
		handler = h;
	}
	
	public  void log(final String msg)
	{

		handler.post(new Runnable()
		{
			
			@Override
			public void run()
			{
				Date now = new Date(System.currentTimeMillis());
				StringBuilder buffer = new StringBuilder();
				buffer.append("[").append(now.getHours()).append(":").append(now.getMinutes()).append(":").append(now.getSeconds()).append("]");
				buffer.append(msg).append("\n");
				statusView.append(buffer);
				
			}
		});
	}
	
	public void clear()
	{
		statusView.setText("");
	}
}
