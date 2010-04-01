/**
 * 
 */
package com.hyk.proxy.gae.client.httpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.compress.CompressorFactory;
import com.hyk.compress.DefaultCompressPreference;
import com.hyk.proxy.gae.client.config.Config;
import com.hyk.proxy.gae.client.config.XmppAccount;
import com.hyk.proxy.gae.client.rpc.HttpClientRpcChannel;
import com.hyk.proxy.gae.client.rpc.XmppRpcChannel;
import com.hyk.proxy.gae.client.util.ClientUtils;
import com.hyk.proxy.gae.common.HttpServerAddress;
import com.hyk.proxy.gae.common.XmppAddress;
import com.hyk.proxy.gae.common.service.FetchService;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.RpcException;
import com.hyk.rpc.core.constant.RpcConstants;
import com.hyk.rpc.core.service.NameService;
import com.hyk.rpc.core.transport.RpcChannel;
import com.hyk.util.buffer.ByteArrayPoolDaemon;

/**
 * @author yinqiwen
 * 
 */
public class HttpServer
{
	protected static Logger	logger	= LoggerFactory.getLogger(HttpServer.class);
	
	private List<RpcChannel> rpcChannels = new LinkedList<RpcChannel>();

	protected RPC createXmppRpc(XmppAccount account,Executor workerExecutor, Properties initProps) throws XMPPException, RpcException
	{
		XmppRpcChannel xmppRpcchannle = new XmppRpcChannel(workerExecutor, account);
		rpcChannels.add(xmppRpcchannle);
		return new RPC(xmppRpcchannle, initProps);
	}

	protected RPC createHttpRpc(String appid, Executor workerExecutor, Properties initProps) throws IOException, RpcException
	{
		HttpServerAddress remoteAddress = new HttpServerAddress(appid + ".appspot.com", "/fetchproxy");
		//HttpServerAddress remoteAddress = new HttpServerAddress("localhost",8888, "/fetchproxy");
		HttpClientRpcChannel httpCleintRpcchannle = new HttpClientRpcChannel(workerExecutor, remoteAddress, 2048000);
		rpcChannels.add(httpCleintRpcchannle);
		return new RPC(httpCleintRpcchannle, initProps);
	}

	protected FetchService initXmppFetchService(String appid, RPC rpc) throws XMPPException
	{
		NameService serv = rpc.getRemoteNaming(new XmppAddress(appid + "@appspot.com"));
		FetchService fetchService = (FetchService)serv.lookup("fetch");
		return fetchService;
	}

	protected FetchService initHttpFetchService(String appid, RPC rpc) throws XMPPException
	{
		HttpServerAddress remoteAddress = new HttpServerAddress(appid + ".appspot.com", "/fetchproxy");
		NameService serv = rpc.getRemoteNaming(remoteAddress);
		return (FetchService)serv.lookup("fetch");
	}
	
	public void start() throws IOException, RpcException
	{
		List<FetchService> fetchServices = new LinkedList<FetchService>();
		SSLContext sslContext = null;
		Config config = Config.getInstance();
		
		Executor bossExecutor = Executors.newCachedThreadPool();
		//Executor workerExecutor = Executors.newFixedThreadPool(50);
		Executor workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(config.getThreadPoolSize(), 0, 0);
		
		DefaultCompressPreference.init(CompressorFactory.getCompressor(config.getCompressorType()), config.getCompressorTrigger());
		Properties initProps = new Properties();
		initProps.setProperty(RpcConstants.SESSIN_TIMEOUT, Integer.toString(config.getSessionTimeout()));
		initProps.setProperty(RpcConstants.COMPRESS_PREFER, "com.hyk.compress.DefaultCompressPreference");
		try
		{
			sslContext = ClientUtils.initSSLContext();
			
			List<String> appids = config.getAppids();
			//Only effect when HTTP mode is disable
			if(config.isXmppEnable() && !config.isHttpEnable())
			{
				List<XmppAccount> xmppAccounts = config.getAccounts();
				for(XmppAccount account : xmppAccounts)
				{
					RPC rpc = createXmppRpc(account, workerExecutor, initProps);
					//rpc.setSessionTimeout(config.getSessionTimeout());
					for(String appid : appids)
					{
						try
						{
							fetchServices.add(initXmppFetchService(appid, rpc));
						}
						catch(Exception e)
						{
							logger.error("Failed to retireve xmpp remote service, please check your configuration.", e);
						}

					}
				}
			}

			if(config.isHttpEnable())
			{
				for(String appid : appids)
				{					
					try
					{
						RPC rpc = createHttpRpc(appid, workerExecutor, initProps);
						fetchServices.add(initHttpFetchService(appid, rpc));
					}
					catch(Exception e)
					{
						logger.error("Failed to retireve http remote service, please check your configuration.", e);
					}
					
				}
			}
		}
		catch(Exception e)
		{
			logger.error("Failed to retireve remote service, please check your configuration.", e);
		}
		if(fetchServices.isEmpty())
		{
			logger.error("No fetch service found, please check configuration again.");
			System.exit(-1);
		}
		if(logger.isInfoEnabled())
		{
			logger.info("Found " + fetchServices.size() + " remote fetch service for this proxy.");
		}
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new HttpServerPipelineFactory(fetchServices, workerExecutor, sslContext, this));
		bootstrap.bind(new InetSocketAddress(InetAddress.getByName(config.getLocalServerHost()), config.getLocalServerPort()));
		
		ByteArrayPoolDaemon.start();
	}
	
	public void stop()
	{
		for(RpcChannel rpcChannel:rpcChannels)
		{
			rpcChannel.close();
		}
		System.exit(1);
	}

}
