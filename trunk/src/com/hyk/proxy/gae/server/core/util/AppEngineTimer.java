/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppEngineTimer.java 
 *
 * @author yinqiwen [ 2010-3-28 | обнГ09:46:57 ]
 *
 */
package com.hyk.proxy.gae.server.core.util;

import com.hyk.timer.Timer;
import com.hyk.timer.TimerTask;

/**
 *
 */
public class AppEngineTimer implements Timer
{

	@Override
	public TimerTask schedule(Runnable task, long delay)
	{
		//just return null since never invoked
		return null;
	}

}
