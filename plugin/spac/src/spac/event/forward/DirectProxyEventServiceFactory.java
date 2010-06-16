/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventServiceFactory.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:13:49 AM ]
 *
 */
package spac.event.forward;

import com.hyk.proxy.client.framework.event.HttpProxyEventService;

/**
 *
 */
public class DirectProxyEventServiceFactory extends ForwardProxyEventServiceFactory
{
	public static final String NAME = "DIRECT";
	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		return new DirectProxyEventService(factory);
	}

	@Override
	public void close()
	{

	}

	@Override
	public String getName()
	{
		return NAME;
	}

}
