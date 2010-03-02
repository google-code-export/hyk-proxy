/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpClientRpcChannel.java 
 *
 * @author yinqiwen [ Jan 28, 2010 | 5:41:08 PM ]
 *
 */
package com.hyk.proxy.gae.client.rpc;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.SocketChannel;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.hyk.proxy.gae.client.config.Config;
import com.hyk.proxy.gae.common.HttpServerAddress;
import com.hyk.proxy.gae.common.service.FetchService;
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

	private HttpServerAddress				remoteAddress;

	private ClientSocketChannelFactory		factory			= new NioClientSocketChannelFactory(threadPool, threadPool);

	private HttpClientSocketChannelSelector	clientChannelSelector;

	class HttpClientSocketChannel
	{
		SocketChannel	socketChannel;

		public HttpClientSocketChannel()
		{
			this.socketChannel = null;
		}

		public synchronized SocketChannel getSocketChannel()
		{
			if(null == socketChannel || !socketChannel.isConnected())
			{
				socketChannel = connectProxyServer();
			}
			return socketChannel;
		}

		public void close()
		{
			if(socketChannel != null && socketChannel.isOpen())
			{
				socketChannel.close();
			}
		}
	}

	class HttpClientSocketChannelSelector
	{
		private List<HttpClientSocketChannel>	clientChannels;
		private int								cursor;

		public HttpClientSocketChannelSelector(List<HttpClientSocketChannel> clientChannels)
		{
			super();
			this.clientChannels = clientChannels;
		}

		public synchronized HttpClientSocketChannel select()
		{
			if(cursor >= clientChannels.size())
			{
				cursor = 0;
			}
			HttpClientSocketChannel channle = clientChannels.get(cursor++);
			return channle;
		}

		public void close()
		{
			for(HttpClientSocketChannel channel : clientChannels)
			{
				channel.close();
			}
		}
	}

	public HttpClientRpcChannel(Executor threadPool, HttpServerAddress remoteAddress, final int maxMessageSize) throws IOException
	{
		super(threadPool);
		this.remoteAddress = remoteAddress;
		start();
		setMaxMessageSize(maxMessageSize);
		List<HttpClientSocketChannel> clientChannels = new ArrayList<HttpClientSocketChannel>();
		int maxHttpConnectionSize = Config.getInstance().getHttpConnectionPoolSize();
		for(int i = 0; i < maxHttpConnectionSize; i++)
		{
			clientChannels.add(new HttpClientSocketChannel());
		}
		clientChannelSelector = new HttpClientSocketChannelSelector(clientChannels);
	}

	private synchronized SocketChannel connectProxyServer()
	{

		if(logger.isDebugEnabled())
		{
			logger.debug("Connect remote proxy server.");
		}
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(maxMessageSize));
		pipeline.addLast("encoder", new HttpRequestEncoder());
		pipeline.addLast("handler", responseHandler);
		SocketChannel channel = factory.newChannel(pipeline);
		ChannelFuture future = channel.connect(new InetSocketAddress(remoteAddress.getHost(), remoteAddress.getPort())).awaitUninterruptibly();
		if(!future.isSuccess())
		{
			logger.error("Failed to connect proxy server.", future.getCause());
		}
		return channel;
	}

	@Override
	public void close()
	{
		super.close();
		clientChannelSelector.close();
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
		if(logger.isDebugEnabled())
		{
			logger.debug("send  data to:" + data.address.toPrintableString());
		}
		HttpClientSocketChannel clientChannel = clientChannelSelector.select();

		if(clientChannel.getSocketChannel().isConnected())
		{
			HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, remoteAddress.getPath());
			request.setHeader("Host", remoteAddress.getHost() + ":" + remoteAddress.getPort());
			request.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
			//request.setHeader(HttpHeaders.Names.CONNECTION, "close");
			// request.setHeader(HttpHeaders.Names.TRANSFER_ENCODING,
			// HttpHeaders.Values.CHUNKED);
			request.setHeader(HttpHeaders.Names.CONTENT_TRANSFER_ENCODING, HttpHeaders.Values.BINARY);
			request.setHeader(HttpHeaders.Names.USER_AGENT, "hyk-proxy-client");
			request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/octet-stream");

			ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data.content.buffer());
			request.setHeader("Content-Length", String.valueOf(buffer.readableBytes()));

			request.setContent(buffer);
			clientChannel.getSocketChannel().write(request).awaitUninterruptibly();
		}
		else
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Channel is close.");
			}
		}

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

	@ChannelPipelineCoverage("one")
	class HttpResponseHandler extends SimpleChannelUpstreamHandler
	{
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Connection closed.");
			}
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
		{
			if(!(e.getMessage() instanceof HttpResponse))
			{
				HttpChunk chunk = (HttpChunk)e.getMessage();
				if(logger.isDebugEnabled())
				{
					logger.debug("Recv chunk:" + chunk.getContent().readableBytes());
					// response.g
				}

				return;
			}
			HttpResponse response = (HttpResponse)e.getMessage();

			int bodyLen = (int)response.getContentLength();
			if(logger.isDebugEnabled())
			{
				logger.debug("Recv message:" + response + " with len:" + bodyLen);
				// response.g
			}
			if(bodyLen > 0)
			{
				ByteArray content = ByteArray.allocate(bodyLen);
				// response.g
				ChannelBuffer body = response.getContent();
				body.readBytes(content.buffer());
				content.rewind();
				RpcChannelData recv = new RpcChannelData(content, remoteAddress);
				synchronized(recvList)
				{
					recvList.add(recv);
					recvList.notify();
				}
			}
			else
			{

				if(logger.isDebugEnabled())
				{
					logger.debug("Recv message with no body" + response.getHeaderNames());
				}
			}
		}
	}

}
