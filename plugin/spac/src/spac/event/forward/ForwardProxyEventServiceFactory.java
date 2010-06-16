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

import java.util.concurrent.ExecutorService;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.framework.event.HttpProxyEventService;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.client.framework.status.StatusMonitor;

/**
 *
 */
public class ForwardProxyEventServiceFactory implements HttpProxyEventServiceFactory
{
	public static final String NAME = "FORWARD";
	
	protected ClientSocketChannelFactory	factory;

	public void init(Config config, ExecutorService workerExecutor, StatusMonitor monitor)
	{
		factory = new NioClientSocketChannelFactory(workerExecutor, workerExecutor);
	}

	@Override
	public HttpProxyEventService createHttpProxyEventService()
	{
		return new ForwardProxyEventService(factory);
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
