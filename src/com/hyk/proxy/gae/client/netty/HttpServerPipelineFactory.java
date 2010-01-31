/**
 * 
 */
package com.hyk.proxy.gae.client.netty;

import static org.jboss.netty.channel.Channels.*;

import java.util.List;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.hyk.proxy.gae.common.service.FetchService;

/**
 * @author Administrator
 * 
 */
public class HttpServerPipelineFactory implements ChannelPipelineFactory
{
	private List<FetchService>	fetchServices;
	private Executor workerExecutor;
	private SSLContext sslContext;

	public HttpServerPipelineFactory(List<FetchService> fetchServices, Executor workerExecutor, SSLContext sslContext)
	{
		this.fetchServices = fetchServices;
		this.workerExecutor = workerExecutor;
		this.sslContext = sslContext;
	}

	public ChannelPipeline getPipeline() throws Exception
	{
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();

		// Uncomment the following line if you want HTTPS
		// SSLEngine engine =
		// SecureChatSslContextFactory.getServerContext().createSSLEngine();
		// engine.setUseClientMode(false);
		// pipeline.addLast("ssl", new SslHandler(engine));
		pipeline.addLast("decoder", new HttpRequestDecoder());
		// Uncomment the following line if you don't want to handle HttpChunks.
		pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("handler", new HttpRequestHandler(sslContext, pipeline, fetchServices, workerExecutor));
		return pipeline;
	}
}
