/**
 * 
 */
package com.hyk.proxy.plugin.phptunnel;

import static org.jboss.netty.channel.Channels.pipeline;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sbbi.upnp.impls.InternetGatewayDevice;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.common.Misc;
import com.hyk.proxy.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.framework.plugin.PluginAdmin;
import com.hyk.proxy.framework.plugin.PluginContext;
import com.hyk.proxy.framework.plugin.TUIPlugin;
import com.hyk.proxy.plugin.phptunnel.config.PhpTunnelApplicationConfig;
import com.hyk.proxy.plugin.phptunnel.event.PhpTunnelClientProxyEventServiceFactory;
import com.hyk.proxy.plugin.phptunnel.event.PhpTunnelLocalServerHandler;
import com.hyk.util.net.NetUtil;

/**
 * @author wqy
 * 
 */
public class PhpTunnelPlugin implements TUIPlugin
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected InternetGatewayDevice IGD = null;
	protected InetSocketAddress serverAddr;
	private PluginContext context;
	private InetSocketAddress startTunnelServer(final ExecutorService executor)
	        throws Exception
	{

		ServerBootstrap bootstrap = new ServerBootstrap(
		        new NioServerSocketChannelFactory(executor, executor));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{
			@Override
			public ChannelPipeline getPipeline() throws Exception
			{
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("executor", new ExecutionHandler(executor));
				pipeline.addLast("acceptor", new PhpTunnelLocalServerHandler());
				return pipeline;
			}
		});
		Channel serverChannel = bootstrap.bind(new InetSocketAddress(
		        PhpTunnelApplicationConfig.gettunnelPort()));
		return (InetSocketAddress) serverChannel.getLocalAddress();
	}

	private void upnpPortMapping(final InetSocketAddress addr) throws Exception
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Do UPNP mapping.");
		}
		int discoveryTimeout = 5000;
		InternetGatewayDevice[] IGDs = InternetGatewayDevice
		        .getDevices(discoveryTimeout);
		if (IGDs != null)
		{
			// let's the the first device found
			final InternetGatewayDevice testIGD = IGDs[0];
			IGD = testIGD;
			// now let's open the port
			String localHostIP = InetAddress.getLocalHost().getHostAddress();
			// we assume that localHostIP is something else than 127.0.0.1
			boolean mapped = testIGD.addPortMapping(
			        PhpTunnelClientProxyEventServiceFactory.NAME, null,
			        addr.getPort(), addr.getPort(), localHostIP, 0, "TCP");
			if (mapped)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Port " + addr.getPort() + " mapped to "
					        + localHostIP);
				}
				Runtime.getRuntime().addShutdownHook(new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							testIGD.deletePortMapping(null, addr.getPort(),
							        "TCP");
						}
						catch (Exception e)
						{
							logger.error("Failed to unmapping ports.", e);
						}

					}
				});

			}
		}
	}

	@Override
	public void onActive(PluginContext context) throws Exception
	{
		final ExecutorService executor = Misc.getGlobalThreadPool();
		HttpProxyEventServiceFactory.Registry
		        .register(new PhpTunnelClientProxyEventServiceFactory(executor,
		        		PhpTunnelApplicationConfig.gettunnelPort()));
		executor.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					serverAddr = startTunnelServer(executor);
					//if (NetUtil.isPrivateIP(serverAddr.getAddress()
					//        .getHostAddress()))
					{
						upnpPortMapping(serverAddr);
					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

	}

	@Override
	public void onLoad(PluginContext context) throws Exception
	{
		this.context = context;
	}

	@Override
	public void onDeactive(PluginContext context) throws Exception
	{
		if (null != IGD)
		{
			IGD.deletePortMapping(null, serverAddr.getPort(), "TCP");
		}

	}

	@Override
	public void onUnload(PluginContext context) throws Exception
	{
		
	}

	@Override
    public void onConfig()
    {
		File home = context.getHome();
		try
        {
	        Desktop.getDesktop().browse(new File(home, "deploy").toURI());
        }
        catch (IOException e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    
    }

	@Override
    public PluginAdmin getAdmin()
    {
	    // TODO Auto-generated method stub
	    return null;
    }

}
