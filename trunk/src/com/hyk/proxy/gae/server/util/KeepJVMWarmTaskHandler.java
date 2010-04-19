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

import com.hyk.proxy.gae.server.core.service.BandwidthStatisticsServiceImpl;

/**
 *
 */
public class KeepJVMWarmTaskHandler extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		BandwidthStatisticsServiceImpl.storeStatResults();
		resp.setStatus(200);
		resp.getWriter().write("OK");
	}
}
