/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: KeepJVMWarmTaskHandler.java 
 *
 * @author qiying.wang [ Apr 16, 2010 | 2:08:29 PM ]
 *
 */
package com.hyk.proxy.server.gae.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hyk.proxy.server.gae.rpc.service.BandwidthStatisticsServiceImpl;


/**
 *
 */
public class BandwidthStatHandler extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		BandwidthStatisticsServiceImpl.storeStatResults();
		resp.setStatus(200);
		resp.getWriter().write("OK");
	}
}
