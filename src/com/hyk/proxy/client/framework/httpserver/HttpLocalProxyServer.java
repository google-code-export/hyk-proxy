/**
 * 
 */
package com.hyk.proxy.client.framework.httpserver;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.config.Config.SimpleSocketAddress;
import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;


/**
 * @author yinqiwen
 * 
 */
public class HttpLocalProxyServer
{
	protected static Logger	logger	= LoggerFactory.getLogger(HttpLocalProxyServer.class);
	
	//private List<RpcChannel> rpcChannels = new LinkedList<RpcChannel>();
    private ServerBootstrap bootstrap;
    //private final ThreadPoolExecutor workerExecutor;
    private Channel serverChannel;
    private Executor bossExecutor = Executors.newCachedThreadPool();
	
    public HttpLocalProxyServer(SimpleSocketAddress address, final ThreadPoolExecutor workerExecutor, final HttpProxyEventServiceFactory eventServiceFactory) 
    {
    	
		//this.workerExecutor = workerExecutor;
    	bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));	
    	// Set up the event pipeline factory.
    	//httpServerPipelineFactory = new HttpServerPipelineFactory(workerExecutor, ClientUtils.initSSLContext(), this);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{
			@Override
			public ChannelPipeline getPipeline() throws Exception
			{
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("executor", new ExecutionHandler(workerExecutor));
				pipeline.addLast("decoder", new HttpRequestDecoder());
				// Uncomment the following line if you don't want to handle HttpChunks.
				//pipeline.addLast("aggregator", new HttpChunkAggregator(10485760));
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
				pipeline.addLast("handler", new HttpLocalProxyRequestHandler(eventServiceFactory.createHttpProxyEventService()));
				return pipeline;
			}
		});
		serverChannel = bootstrap.bind(new InetSocketAddress(address.host, address.port));
    }
    
    public void close()
    {
    	serverChannel.close();
    }
}
