/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: XmppAccount.java 
 *
 * @author yinqiwen [ 2010-1-31 | 10:50:02 AM]
 *
 */
package com.hyk.proxy.gae.client.httpserver;

import static org.jboss.netty.channel.Channels.pipeline;

import java.util.List;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

import com.hyk.proxy.gae.client.handler.HttpRequestHandler;
import com.hyk.proxy.gae.client.util.FetchServiceSelector;
import com.hyk.proxy.gae.common.service.FetchService;
import com.hyk.rpc.core.RpcException;

/**
 * @author yinqiwen
 * 
 */
public class HttpServerPipelineFactory implements ChannelPipelineFactory
{
	//private List<FetchService>	fetchServices;
	private Executor workerExecutor;
	private SSLContext sslContext;
	private HttpServer httpServer;
	private FetchServiceSelector selector;

	public HttpServerPipelineFactory(Executor workerExecutor, SSLContext sslContext, HttpServer httpServer) throws RpcException
	{
		this.workerExecutor = workerExecutor;
		this.sslContext = sslContext;
		this.httpServer = httpServer;
		
	}
	
	public void  setFetchServices(List<FetchService> fetchServices) throws RpcException
	{
		this.selector = new FetchServiceSelector(fetchServices);
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
		//pipeline.addLast("aggregator", new HttpChunkAggregator(10485760));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
		pipeline.addLast("handler", new HttpRequestHandler(sslContext, pipeline, selector, httpServer, workerExecutor));
		return pipeline;
	}
}
