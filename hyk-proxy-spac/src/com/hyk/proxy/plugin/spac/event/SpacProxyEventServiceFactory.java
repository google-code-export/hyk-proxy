/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventServiceFactory.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:13:49 AM ]
 *
 */
package com.hyk.proxy.plugin.spac.event;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tykedog.csl.interpreter.CSL;

import com.hyk.proxy.framework.common.Misc;
import com.hyk.proxy.framework.event.HttpProxyEventService;
import com.hyk.proxy.framework.event.HttpProxyEventServiceFactory;

/**
 *
 */
public class SpacProxyEventServiceFactory implements
        HttpProxyEventServiceFactory
{
	public static final String NAME = "SPAC";
	private Logger logger = LoggerFactory.getLogger(getClass());

	CSL csl;

	public SpacProxyEventServiceFactory(CSL csl)
	{
		this.csl = csl;
	}

	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		return new SpacProxyEventService(this);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void init() throws Exception
	{
		Collection<HttpProxyEventServiceFactory> all = HttpProxyEventServiceFactory.Registry
		        .getAllHttpProxyEventServiceFactorys();
		for (HttpProxyEventServiceFactory factory : all)
		{
			if (factory.getName().equals(NAME))
			{
				continue;
			}
			try
			{
				factory.init();
			}
			catch (Exception e)
			{
				logger.warn("Failed to init HttpProxyEventServiceFactory:"
				        + factory.getName(), e);
			}
		}
		Misc.getTrace().info(NAME + " is working now!");
	}

	@Override
	public void destroy() throws Exception
	{
		Collection<HttpProxyEventServiceFactory> all = HttpProxyEventServiceFactory.Registry
		        .getAllHttpProxyEventServiceFactorys();
		for (HttpProxyEventServiceFactory factory : all)
		{
			if (factory.getName().equals(NAME))
			{
				continue;
			}
			try
			{
				factory.destroy();
			}
			catch (Exception e)
			{
				logger.warn("Failed to init HttpProxyEventServiceFactory:"
				        + factory.getName(), e);
			}
		}

	}

}
