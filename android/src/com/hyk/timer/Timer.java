/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Timer.java 
 *
 * @author qiying.wang [ Mar 4, 2010 | 4:37:37 PM ]
 *
 */
package com.hyk.timer;


/**
 *
 */
public interface Timer
{
	  public TimerTask schedule(Runnable task, long delay);
}
