/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpClientRpcChannel.java 
 *
 * @author qiying.wang [ Jan 28, 2010 | 5:41:08 PM ]
 *
 */
package com.hyk.proxy.gae.client.http;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.hyk.rpc.core.transport.AbstractDefaultRpcChannel;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.util.buffer.ByteArray;

/**
 *
 */
public class HttpClientRpcChannel extends AbstractDefaultRpcChannel
{
	private SimpleChannelUpstreamHandler	responseHandler	= new HttpResponseHandler();

	private List<RpcChannelData>			recvList		= new LinkedList<RpcChannelData>();

	private HttpServerAddress				address;

	private Channel							channel;

	public HttpClientRpcChannel(Executor threadPool)
	{
		super(threadPool);

		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{
			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = pipeline();

				pipeline.addLast("decoder", new HttpResponseDecoder());
				pipeline.addLast("encoder", new HttpRequestEncoder());
				pipeline.addLast("handler", responseHandler);
				return pipeline;
			}
		});

		// Start the connection attempt.
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(getRpcChannelAddress().getHost(), getRpcChannelAddress().getPort()));

		// Wait until the connection attempt succeeds or fails.
		channel = future.awaitUninterruptibly().getChannel();
		if(!future.isSuccess())
		{
			future.getCause().printStackTrace();
			bootstrap.releaseExternalResources();
			return;
		}
	}

	@Override
	public HttpServerAddress getRpcChannelAddress()
	{
		return null;
	}

	@Override
	protected RpcChannelData read() throws IOException
	{
		synchronized(recvList)
		{
			if(recvList.isEmpty())
			{
				try
				{
					recvList.wait();
				}
				catch(InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
			return recvList.remove(0);
		}
	}

	@Override
	protected void send(RpcChannelData data) throws IOException
	{
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "");
		request.addHeader("Connection", "keep-alive");
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data.content.buffer());
		request.setContent(buffer);
		channel.write(request);
	}

	@Override
	public boolean isReliable()
	{
		return true;
	}

	public SimpleChannelUpstreamHandler getResponseHandler()
	{
		return responseHandler;
	}

	class HttpResponseHandler extends SimpleChannelUpstreamHandler
	{
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Connection closed.");
			}
			e.getChannel().connect(new InetSocketAddress(address.getHost(), address.getPort()));
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
		{
			HttpResponse response = (HttpResponse)e.getMessage();
			if(logger.isDebugEnabled())
			{
				logger.debug("Recv message.");
			}
			int bodyLen = (int)response.getContentLength();

			if(bodyLen > 0)
			{
				ByteArray content = ByteArray.allocate(bodyLen);
				ChannelBuffer body = response.getContent();
				body.readBytes(content.buffer());
				RpcChannelData recv = new RpcChannelData(content, address);
				synchronized(recvList)
				{
					recvList.add(recv);
					recvList.notify();
				}
			}
		}
	}

}
