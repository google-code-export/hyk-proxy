/**
 * 
 */
package org.hyk.proxy.framework.httpserver;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hyk.proxy.framework.event.HttpProxyEventServiceFactory;
import org.hyk.proxy.framework.httpserver.reverse.HttpsReverseServer;
import org.hyk.proxy.framework.util.SimpleSocketAddress;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.util.net.NetUtil;



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
	
    public HttpLocalProxyServer(SimpleSocketAddress address, final ExecutorService workerExecutor, final HttpProxyEventServiceFactory eventServiceFactory) 
    {	
    	if(NetUtil.isIPV6Address(address.host))
    	{
    		bootstrap = new ServerBootstrap(new OioServerSocketChannelFactory(bossExecutor, workerExecutor));	
    	}
    	else
    	{
    		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));	
    	}
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
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
				pipeline.addLast("handler", new HttpLocalProxyRequestHandler(eventServiceFactory.createHttpProxyEventService()));
				return pipeline;
			}
		});
		try
        {
	        HttpsReverseServer.initSigletonInstance(ClientUtils.getFakeSSLContext("", ""));
        }
        catch (Exception e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		serverChannel = bootstrap.bind(new InetSocketAddress(address.host, address.port));
		//monitor.notifyRunDetail("Local Http Server Running... \nat " + address.host + ":" + address.port);
    }
    
    public void close()
    {
    	serverChannel.close();
    }
}
