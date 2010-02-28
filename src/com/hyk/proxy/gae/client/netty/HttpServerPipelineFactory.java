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
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

import com.hyk.proxy.gae.common.service.FetchService;

/**
 * @author yinqiwen
 * 
 */
public class HttpServerPipelineFactory implements ChannelPipelineFactory
{
	private List<FetchService>	fetchServices;
	private Executor workerExecutor;
	private SSLContext sslContext;
	private HttpServer httpServer;

	public HttpServerPipelineFactory(List<FetchService> fetchServices, Executor workerExecutor, SSLContext sslContext, HttpServer httpServer)
	{
		this.fetchServices = fetchServices;
		this.workerExecutor = workerExecutor;
		this.sslContext = sslContext;
		this.httpServer = httpServer;
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
		pipeline.addLast("executor", new ExecutionHandler(workerExecutor));
		pipeline.addLast("decoder", new HttpRequestDecoder());
		// Uncomment the following line if you don't want to handle HttpChunks.
		pipeline.addLast("aggregator", new HttpChunkAggregator(10485760));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
		pipeline.addLast("handler", new HttpRequestHandler(sslContext, pipeline, fetchServices, workerExecutor, httpServer));
		return pipeline;
	}
}
