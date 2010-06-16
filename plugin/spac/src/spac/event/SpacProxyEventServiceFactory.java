/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventServiceFactory.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:13:49 AM ]
 *
 */
package spac.event;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tykedog.csl.interpreter.CSL;

import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.client.framework.status.StatusMonitor;

/**
 *
 */
public class SpacProxyEventServiceFactory implements HttpProxyEventServiceFactory
{
	public static final String	NAME	= "SPAC";
	private Logger				logger	= LoggerFactory.getLogger(getClass());

	CSL							csl;

	public SpacProxyEventServiceFactory(CSL csl)
	{
		this.csl = csl;
	}

	@Override
	public void init(Config config, ExecutorService workerExecutor, StatusMonitor monitor) throws Exception
	{
		Collection<HttpProxyEventServiceFactory> all = HttpProxyEventServiceFactory.Registry.getAllHttpProxyEventServiceFactorys();
		for(HttpProxyEventServiceFactory factory : all)
		{
			if(factory.getName().equals(NAME))
			{
				continue;
			}
			try
			{
				factory.init(config, workerExecutor, monitor);
			}
			catch(Exception e)
			{
				logger.warn("Failed to init HttpProxyEventServiceFactory:" + factory.getName(), e);
			}
		}
	}

	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		return new SpacProxyEventService(this);
	}

	@Override
	public void close()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getName()
	{
		return NAME;
	}

}
