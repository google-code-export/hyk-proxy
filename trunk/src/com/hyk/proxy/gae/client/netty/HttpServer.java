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

/**
 * @author Administrator
 *
 */
public class HttpServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Executor bossExecutor = Executors.newFixedThreadPool(10);
		Executor workerExecutor = Executors.newFixedThreadPool(20);
		ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                		bossExecutor,
                		workerExecutor));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpServerPipelineFactory());
        Map<String, Object> connectionParams = new HashMap<String, Object>();
        bootstrap.bind(new InetSocketAddress(48100));
        //bootstrap.
	}

}
