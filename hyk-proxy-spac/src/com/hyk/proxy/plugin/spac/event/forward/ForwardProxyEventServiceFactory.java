/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventServiceFactory.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:13:49 AM ]
 *
 */
package com.hyk.proxy.plugin.spac.event.forward;


import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;

import com.hyk.proxy.framework.common.Misc;
import com.hyk.proxy.framework.event.HttpProxyEventService;
import com.hyk.proxy.framework.event.HttpProxyEventServiceFactory;


/**
 *
 */
public class ForwardProxyEventServiceFactory implements HttpProxyEventServiceFactory
{
	public static final String NAME = "FORWARD";
	
	protected ClientSocketChannelFactory	factory;

	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		return new ForwardProxyEventService(factory);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
    public void init() throws Exception
    {
		factory = new OioClientSocketChannelFactory(Misc.getGlobalThreadPool());
    }

	@Override
    public void destroy() throws Exception
    {
	    // TODO Auto-generated method stub
	    
    }

}
