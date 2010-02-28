/**
 * 
 */
package com.hyk.proxy.gae.client.netty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.compress.CompressorFactory;
import com.hyk.compress.CompressorPreference;
import com.hyk.compress.CompressorType;
import com.hyk.proxy.gae.client.config.Config;
import com.hyk.proxy.gae.client.config.XmppAccount;
import com.hyk.proxy.gae.client.rpc.HttpClientRpcChannel;
import com.hyk.proxy.gae.client.rpc.XmppRpcChannel;
import com.hyk.proxy.gae.common.HttpServerAddress;
import com.hyk.proxy.gae.common.XmppAddress;
import com.hyk.proxy.gae.common.service.FetchService;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.service.NameService;
import com.hyk.rpc.core.transport.RpcChannel;

/**
 * @author yinqiwen
 * 
 */
public class HttpServer
{
	protected static Logger	logger	= LoggerFactory.getLogger(HttpServer.class);

	private List<RpcChannel> rpcChannels = new LinkedList<RpcChannel>();
	
	protected SSLContext initSSLContext() throws Exception
	{
		String password = "hykproxy";
		SSLContext sslContext = SSLContext.getInstance("TLS");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(HttpServer.class.getResourceAsStream("/hykproxykeystore"), password.toCharArray());
		kmf.init(ks, password.toCharArray());
		KeyManager[] km = kmf.getKeyManagers();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		TrustManager[] tm = tmf.getTrustManagers();
		sslContext.init(km, tm, null);
		return sslContext;
	}

	protected RPC createXmppRpc(XmppAccount account, Config config, Executor workerExecutor) throws XMPPException
	{
		XmppRpcChannel xmppRpcchannle = new XmppRpcChannel(workerExecutor, account);
		CompressorPreference preference = new CompressorPreference();
		preference.setEnable(true);
		preference.setCompressor(CompressorFactory.getCompressor(config.getCompressorType()));
		preference.setTrigger(config.getCompressorTrigger());
		xmppRpcchannle.setCompressorPreference(preference);
		rpcChannels.add(xmppRpcchannle);
		return new RPC(xmppRpcchannle);
	}

	protected RPC createHttpRpc(String appid, Config config, Executor workerExecutor) throws IOException
	{
		HttpServerAddress remoteAddress = new HttpServerAddress(appid + ".appspot.com", "/fetchproxy");
		//HttpServerAddress remoteAddress = new HttpServerAddress("localhost",8888, "/fetchproxy");
		HttpClientRpcChannel httpCleintRpcchannle = new HttpClientRpcChannel(workerExecutor, remoteAddress, 2048000);
		CompressorPreference preference = new CompressorPreference();
		preference.setEnable(true);
		preference.setCompressor(CompressorFactory.getCompressor(config.getCompressorType()));
		preference.setTrigger(config.getCompressorTrigger());
		httpCleintRpcchannle.setCompressorPreference(preference);
		rpcChannels.add(httpCleintRpcchannle);
		return new RPC(httpCleintRpcchannle);
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
	
	public void start() throws IOException
	{
		List<FetchService> fetchServices = new LinkedList<FetchService>();
		SSLContext sslContext = null;
		Config config = Config.getInstance();
		
		Executor bossExecutor = Executors.newCachedThreadPool();
		//Executor workerExecutor = Executors.newFixedThreadPool(50);
		Executor workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(50, 0, 0);
		
		try
		{
			sslContext = initSSLContext();
			
			List<String> appids = config.getAppids();
			//Only effect when HTTP mode is disable
			if(config.isXmppEnable() && !config.isHttpEnable())
			{
				List<XmppAccount> xmppAccounts = config.getAccounts();
				for(XmppAccount account : xmppAccounts)
				{
					RPC rpc = createXmppRpc(account, config, workerExecutor);
					rpc.setSessionTimeout(config.getSessionTimeout());
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
						RPC rpc = createHttpRpc(appid, config, workerExecutor);
						rpc.setSessionTimeout(config.getSessionTimeout());
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
