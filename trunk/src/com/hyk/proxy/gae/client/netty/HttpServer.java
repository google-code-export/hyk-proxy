/**
 * 
 */
package com.hyk.proxy.gae.client.netty;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.client.http.HttpClientRpcChannel;
import com.hyk.proxy.gae.client.xmpp.XmppRpcChannel;
import com.hyk.proxy.gae.common.HttpServerAddress;
import com.hyk.proxy.gae.common.XmppAddress;
import com.hyk.proxy.gae.common.service.FetchService;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.service.NameService;

/**
 * @author Administrator
 * 
 */
public class HttpServer
{
	protected  static Logger	logger	= LoggerFactory.getLogger(HttpServer.class);
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

		FetchService fetchService = null;
		try
		{
//			XmppRpcChannel xmppRpcchannle = new XmppRpcChannel(Executors.newFixedThreadPool(10), "yinqiwen@gmail.com", "Kingwon1983");
//			RPC rpc = new RPC(xmppRpcchannle);
//			NameService serv = rpc.getRemoteNaming(new XmppAddress("hykserver@appspot.com"));
//			fetchService = (FetchService)serv.lookup("fetch");
			
			//HttpServerAddress remoteAddress = new HttpServerAddress("127.0.0.1", 8888, "/fetchproxy");
			HttpServerAddress remoteAddress = new HttpServerAddress("hykserver.appspot.com", "/fetchproxy");
			HttpClientRpcChannel httpCleintRpcchannle = new HttpClientRpcChannel(Executors.newFixedThreadPool(10), remoteAddress, 1048576);
			//httpCleintRpcchannle.setMaxMessageSize(10240000);
			RPC rpc = new RPC(httpCleintRpcchannle);
			NameService serv = rpc.getRemoteNaming(remoteAddress);
			fetchService = (FetchService)serv.lookup("fetch");
		}
		catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Executor bossExecutor = Executors.newFixedThreadPool(50);
		Executor workerExecutor = Executors.newFixedThreadPool(50);
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new HttpServerPipelineFactory(fetchService));
		Map<String, Object> connectionParams = new HashMap<String, Object>();
		bootstrap.bind(new InetSocketAddress(48100));
		// bootstrap.
	}

}
