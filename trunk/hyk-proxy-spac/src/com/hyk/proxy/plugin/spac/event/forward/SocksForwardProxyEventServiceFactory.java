/**
 * This file is part of the hyk-proxy-spac project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SocksForwardProxyEventServiceFactory.java 
 *
 * @author yinqiwen [ 2010-12-18 | ÏÂÎç09:23:24 ]
 *
 */
package com.hyk.proxy.plugin.spac.event.forward;

import com.hyk.proxy.framework.event.HttpProxyEventService;
import com.hyk.proxy.framework.event.HttpProxyEventServiceFactory;

/**
 *
 */
public class SocksForwardProxyEventServiceFactory implements
        HttpProxyEventServiceFactory
{

	public static final String NAME = "SOCKS";
	
	private static SocksForwardProxyEventServiceFactory instance = new SocksForwardProxyEventServiceFactory();

	public static SocksForwardProxyEventServiceFactory getInstance()
	{
		return instance;
	}

	private SocksForwardProxyEventServiceFactory()
	{
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void init() throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		return new SocksForwardProxyEventService();
	}

	@Override
	public void destroy() throws Exception
	{
		// TODO Auto-generated method stub

	}

}
