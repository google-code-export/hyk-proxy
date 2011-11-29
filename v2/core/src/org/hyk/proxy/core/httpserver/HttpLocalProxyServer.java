/**
 * 
 */
package org.hyk.proxy.core.httpserver;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.arch.util.NetworkHelper;
import org.hyk.proxy.core.config.CoreConfiguration;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yinqiwen
 * 
 */
public class HttpLocalProxyServer
{
	protected static Logger logger = LoggerFactory
	        .getLogger(HttpLocalProxyServer.class);

	// private List<RpcChannel> rpcChannels = new LinkedList<RpcChannel>();
	private ServerBootstrap bootstrap;
	// private final ThreadPoolExecutor workerExecutor;
	private Channel serverChannel;
	private Executor bossExecutor = Executors.newCachedThreadPool();

	public HttpLocalProxyServer()
	{
		CoreConfiguration cfg = CoreConfiguration.getInstance();
		String host = cfg.getLocalProxyServerAddress().host;
		int port = cfg.getLocalProxyServerAddress().port;
		int threadpoolSize = cfg.getThreadPoolSize();
		final ThreadPoolExecutor workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(threadpoolSize, 0, 0);
		if (NetworkHelper.isIPV6Address(host))
		{
			bootstrap = new ServerBootstrap(new OioServerSocketChannelFactory(
			        bossExecutor, workerExecutor));
		}
		else
		{
			bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
			        bossExecutor, workerExecutor));
		}
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{
			@Override
			public ChannelPipeline getPipeline() throws Exception
			{
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("executor", new ExecutionHandler(
				        workerExecutor));
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
				pipeline.addLast("handler", new HttpLocalProxyRequestHandler());
				return pipeline;
			}
		});
		serverChannel = bootstrap.bind(new InetSocketAddress(host, port));
	}

	public void close()
	{
		serverChannel.close();
	}
	
}
