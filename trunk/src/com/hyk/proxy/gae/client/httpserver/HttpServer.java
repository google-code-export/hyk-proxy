/**
 * 
 */
package com.hyk.proxy.gae.client.httpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.client.config.AppIdAuth;
import com.hyk.proxy.gae.client.config.Config;
import com.hyk.proxy.gae.client.config.XmppAccount;
import com.hyk.proxy.gae.client.util.ClientUtils;
import com.hyk.proxy.gae.common.Constants;
import com.hyk.proxy.gae.common.auth.UserInfo;
import com.hyk.proxy.gae.common.extension.ExtensionsLauncher;
import com.hyk.proxy.gae.common.http.message.HttpServerAddress;
import com.hyk.proxy.gae.common.service.FetchService;
import com.hyk.proxy.gae.common.service.RemoteServiceManager;
import com.hyk.proxy.gae.common.xmpp.XmppAddress;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.RpcException;
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
    	ExtensionsLauncher.init();
    	Config config = Config.getInstance();
    	Executor bossExecutor = Executors.newCachedThreadPool();
		workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(config.getThreadPoolSize(), 0, 0);
    	bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));	
    	// Set up the event pipeline factory.
    	httpServerPipelineFactory = new HttpServerPipelineFactory(workerExecutor, ClientUtils.initSSLContext(), this);
		bootstrap.setPipelineFactory(httpServerPipelineFactory);
    	bootstrap.bind(new InetSocketAddress(InetAddress.getByName(config.getLocalServerHost()), config.getLocalServerPort()));
    }
    

	protected FetchService initXmppFetchService(AppIdAuth appid, RPC rpc) throws XMPPException
	{
		RemoteServiceManager remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME, new XmppAddress(appid.getAppid() + "@appspot.com"));
		UserInfo info = new UserInfo();
		info.setEmail(appid.getEmail());
		info.setPasswd(appid.getPasswd());
		return remoteServiceManager.getFetchService(info);
	}

	protected FetchService initHttpFetchService(AppIdAuth appid, RPC rpc) throws XMPPException
	{
		HttpServerAddress remoteAddress = new HttpServerAddress(appid.getAppid() + ".appspot.com",  Constants.HTTP_INVOKE_PATH);
		RemoteServiceManager remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME, remoteAddress);
		UserInfo info = new UserInfo();
		info.setEmail(appid.getEmail());
		info.setPasswd(appid.getPasswd());
		return remoteServiceManager.getFetchService(info);
	}
	
	public String start() throws IOException, RpcException
	{
		
		Config config = Config.getInstance();
		List<FetchService> fetchServices = new LinkedList<FetchService>();
		//DefaultCompressPreference.init(CompressorFactory.getCompressor(config.getCompressorType()), config.getCompressorTrigger());
		//Properties initProps = new Properties();
		//initProps.setProperty(RpcConstants.SESSIN_TIMEOUT, Integer.toString(config.getSessionTimeout()));
		//initProps.setProperty(RpcConstants.COMPRESS_PREFER, DefaultCompressPreference.class.getName());
		try
		{
			
			List<AppIdAuth> appids = config.getAppids();
			//Only effect when HTTP mode is disable
			if(config.isXmppEnable() && !config.isHttpEnable())
			{
				List<XmppAccount> xmppAccounts = config.getAccounts();
				for(XmppAccount account : xmppAccounts)
				{
					//RPC rpc = createXmppRpc(account, workerExecutor, initProps);
					RPC rpc = ClientUtils.createXmppRPC(account, workerExecutor);
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
						//RPC rpc = createHttpRpc(appid, workerExecutor, initProps);
						RPC rpc = ClientUtils.createHttpRPC(appid.getAppid(), workerExecutor);
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
