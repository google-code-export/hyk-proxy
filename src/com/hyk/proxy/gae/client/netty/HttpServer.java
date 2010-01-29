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

import com.hyk.proxy.gae.client.xmpp.XmppRpcChannel;
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

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

		FetchService fetchService = null;
		try
		{
			XmppRpcChannel rpcchannle = new XmppRpcChannel(Executors.newFixedThreadPool(10), "yinqiwen@gmail.com", "Kingwon1983");
			RPC rpc = new RPC(rpcchannle);
			NameService serv = rpc.getRemoteNaming(new XmppAddress("hykserver@appspot.com"));
			fetchService = (FetchService)serv.lookup("fetch");
		}
		catch(XMPPException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Executor bossExecutor = Executors.newFixedThreadPool(10);
		Executor workerExecutor = Executors.newFixedThreadPool(20);
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new HttpServerPipelineFactory(fetchService));
		Map<String, Object> connectionParams = new HashMap<String, Object>();
		bootstrap.bind(new InetSocketAddress(48100));
		// bootstrap.
	}

}
