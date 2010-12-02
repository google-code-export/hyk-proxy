/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventService.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:14:39 AM ]
 *
 */
package com.hyk.proxy.plugin.seattle.event;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.common.Misc;
import com.hyk.proxy.framework.event.HttpProxyEvent;
import com.hyk.proxy.framework.event.tunnel.AbstractTunnelProxyEventService;
import com.hyk.proxy.framework.util.ListSelector;
import com.hyk.proxy.framework.util.SimpleSocketAddress;

/**
 *
 */
class SeattleProxyEventService extends AbstractTunnelProxyEventService
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private ClientSocketChannelFactory factory;

	private ListSelector<SimpleSocketAddress> selector;

	// private HttpProxyEventServiceStateListener listener;

	public SeattleProxyEventService(ClientSocketChannelFactory factory,
	        ListSelector<SimpleSocketAddress> selector)
	{
		this.factory = factory;
		this.selector = selector;
	}

	@Override
	protected void getRemoteChannel(HttpProxyEvent event, CallBack callack)
	        throws Exception
	{
		if (null != remoteChannel && remoteChannel.isOpen())
		{
			callack.callback(remoteChannel);
			return;
		}
		
		ChannelFuture future = null;
		SimpleSocketAddress addr = null;
		do
		{
			if (null != addr)
			{
				logger.error("Remote Seattle Server-" + addr + " is not reachable now.");
				Misc.getTrace().error("Remote Seattle Server-" + addr + " is not reachable now.");
				selector.remove(addr);
			}
			addr = selector.select();
			if (null == addr)
			{
				callack.callback(null);
				return;
			}
			ChannelPipeline pipeline = pipeline();
			pipeline.addLast("empty", new EmptyHandler());
			remoteChannel = factory.newChannel(pipeline);
			future = remoteChannel.connect(
			        new InetSocketAddress(addr.host, addr.port)).await();
		}
		while (!future.isSuccess());
		callack.callback(remoteChannel);
	}
}
