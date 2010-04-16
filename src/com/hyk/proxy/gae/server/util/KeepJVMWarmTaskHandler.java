/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: KeepJVMWarmTaskHandler.java 
 *
 * @author qiying.wang [ Apr 16, 2010 | 2:08:29 PM ]
 *
 */
package com.hyk.proxy.gae.server.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.hyk.proxy.gae.server.config.Config;

/**
 *
 */
public class KeepJVMWarmTaskHandler extends HttpServlet
{
	public static void addKeepJVMWarmTask()
	{
		Queue queue = QueueFactory.getQueue("keep-jvm-warm");
		TaskOptions task = TaskOptions.Builder.countdownMillis(Config.getInstance().getKeepWarmPeriod()).method(Method.GET);
		queue.add(task);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		addKeepJVMWarmTask();
		resp.setStatus(200);
		resp.getWriter().write("OK");
	}
}
