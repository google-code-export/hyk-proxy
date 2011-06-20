/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: StandardTimer.java 
 *
 * @author qiying.wang [ Mar 4, 2010 | 4:38:11 PM ]
 *
 */
package com.hyk.timer.standard;

import com.hyk.timer.Timer;
import com.hyk.timer.TimerTask;

/**
 *
 */
public class StandardTimer implements Timer
{
	private java.util.Timer timer;
	
	public StandardTimer()
	{
		timer = new java.util.Timer("Hyk-RPC-Timer", true);
	}
	@Override
	public TimerTask schedule(final Runnable task, long delay)
	{
		StandardTimerTask ret = new StandardTimerTask()
		{
			@Override
			public void run()
			{
				task.run();
			}
			
		};
		timer.schedule(ret, delay);
		return ret;
		
	}
}
