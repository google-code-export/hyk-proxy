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
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.SSLContext;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.compress.CompressorFactory;
import com.hyk.compress.DefaultCompressPreference;
import com.hyk.proxy.gae.client.config.AppIdAuth;
import com.hyk.proxy.gae.client.config.Config;
import com.hyk.proxy.gae.client.config.XmppAccount;
import com.hyk.proxy.gae.client.rpc.HttpClientRpcChannel;
import com.hyk.proxy.gae.client.rpc.XmppRpcChannel;
import com.hyk.proxy.gae.client.util.ClientUtils;
import com.hyk.proxy.gae.common.auth.UserInfo;
import com.hyk.proxy.gae.common.http.message.HttpServerAddress;
import com.hyk.proxy.gae.common.service.FetchService;
import com.hyk.proxy.gae.common.service.RemoteServiceManager;
import com.hyk.proxy.gae.common.xmpp.XmppAddress;
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
    private ServerBootstrap bootstrap;
    private ThreadPoolExecutor workerExecutor;
    private HttpServerPipelineFactory httpServerPipelineFactory = null;
    //private List<FetchService> fetchServices = new LinkedList<FetchService>();
	
    public HttpServer() throws RpcException, Exception
    {
    	Config config = Config.getInstance();
    	Executor bossExecutor = Executors.newCachedThreadPool();
		workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(config.getThreadPoolSize(), 0, 0);
    	bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));	
    	// Set up the event pipeline factory.
    	httpServerPipelineFactory = new HttpServerPipelineFactory(workerExecutor, ClientUtils.initSSLContext(), this);
		bootstrap.setPipelineFactory(httpServerPipelineFactory);
    	bootstrap.bind(new InetSocketAddress(InetAddress.getByName(config.getLocalServerHost()), config.getLocalServerPort()));
    }
    
	protected RPC createXmppRpc(XmppAccount account,Executor workerExecutor, Properties initProps) throws XMPPException, RpcException
	{
		XmppRpcChannel xmppRpcchannle = new XmppRpcChannel(workerExecutor, account);
		rpcChannels.add(xmppRpcchannle);
		return new RPC(xmppRpcchannle, initProps);
	}

	protected RPC createHttpRpc(AppIdAuth appid, Executor workerExecutor, Properties initProps) throws IOException, RpcException
	{
		
		HttpServerAddress remoteAddress = new HttpServerAddress(appid.getAppid() + ".appspot.com", "/fetchproxy");
		//HttpServerAddress remoteAddress = new HttpServerAddress("localhost",8888, "/fetchproxy");
		HttpClientRpcChannel httpCleintRpcchannle = new HttpClientRpcChannel(workerExecutor, remoteAddress);
		rpcChannels.add(httpCleintRpcchannle);
		return new RPC(httpCleintRpcchannle, initProps);
	}

	protected FetchService initXmppFetchService(AppIdAuth appid, RPC rpc) throws XMPPException
	{
		RemoteServiceManager remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME, new XmppAddress(appid.getAppid() + "@appspot.com"));
		//NameService serv = rpc.getRemoteNaming(new XmppAddress(appid.getAppid() + "@appspot.com"));
		//FetchService fetchService = (FetchService)serv.lookup("fetch");
		UserInfo info = new UserInfo();
		info.setEmail(appid.getEmail());
		info.setPasswd(appid.getPasswd());
		return remoteServiceManager.getFetchService(info);
	}

	protected FetchService initHttpFetchService(AppIdAuth appid, RPC rpc) throws XMPPException
	{
		//RemoteServiceManager remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME, new HttpServerAddress("localhost",
		//		8888, "/fetchproxy"));
		HttpServerAddress remoteAddress = new HttpServerAddress(appid.getAppid() + ".appspot.com", "/fetchproxy");
		RemoteServiceManager remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME, remoteAddress);
		//NameService serv = rpc.getRemoteNaming(remoteAddress);
		//return (FetchService)serv.lookup("fetch");
		UserInfo info = new UserInfo();
		info.setEmail(appid.getEmail());
		info.setPasswd(appid.getPasswd());
		return remoteServiceManager.getFetchService(info);
	}
	
	public String start() throws IOException, RpcException
	{
		
		Config config = Config.getInstance();
		List<FetchService> fetchServices = new LinkedList<FetchService>();
		DefaultCompressPreference.init(CompressorFactory.getCompressor(config.getCompressorType()), config.getCompressorTrigger());
		Properties initProps = new Properties();
		initProps.setProperty(RpcConstants.SESSIN_TIMEOUT, Integer.toString(config.getSessionTimeout()));
		initProps.setProperty(RpcConstants.COMPRESS_PREFER, "com.hyk.compress.DefaultCompressPreference");
		try
		{
			
			List<AppIdAuth> appids = config.getAppids();
			//Only effect when HTTP mode is disable
			if(config.isXmppEnable() && !config.isHttpEnable())
			{
				List<XmppAccount> xmppAccounts = config.getAccounts();
				for(XmppAccount account : xmppAccounts)
				{
					RPC rpc = createXmppRpc(account, workerExecutor, initProps);
					for(AppIdAuth appid : appids)
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
				for(AppIdAuth appid : appids)
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
			return "No fetch service found.";
			//System.exit(-1);
		}
		
		
		ByteArrayPoolDaemon.startCleanTask(30);
		httpServerPipelineFactory.setFetchServices(fetchServices);
		return fetchServices.size() + " fetch service is working.";
	}
	
	public void stop()
	{
		for(RpcChannel rpcChannel:rpcChannels)
		{
			rpcChannel.close();
		}
	}

}
