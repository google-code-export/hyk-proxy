/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventServiceFactory.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:13:49 AM ]
 *
 */
package com.hyk.proxy.plugin.seattle.event;

import java.util.concurrent.ExecutorService;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.hyk.proxy.framework.common.Misc;
import com.hyk.proxy.framework.event.HttpProxyEventService;
import com.hyk.proxy.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.framework.util.ListSelector;
import com.hyk.proxy.framework.util.SimpleSocketAddress;
import com.hyk.proxy.plugin.seattle.config.SeattleApplicationConfig;

/**
 *
 */
public class SeattleProxyEventServiceFactory implements
        HttpProxyEventServiceFactory
{

	public static final String NAME = "SeattleGENI";
	private ClientSocketChannelFactory factory;
	private ListSelector<SimpleSocketAddress> selector;

	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		return new SeattleProxyEventService(factory, selector);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void init() throws Exception
	{
		ExecutorService tp = Misc.getGlobalThreadPool();
		factory = new NioClientSocketChannelFactory(tp, tp);
		selector = new ListSelector<SimpleSocketAddress>(
		        SeattleApplicationConfig.getSeattleServerAddress());
		Misc.getTrace().info(NAME + " is working now!");
	}

	@Override
	public void destroy() throws Exception
	{
		// TODO Auto-generated method stub

	}

}
