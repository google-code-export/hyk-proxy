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

import com.hyk.proxy.gae.common.HttpServerAddress;
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
	private SocketChannel					channel;

	public HttpClientRpcChannel(Executor threadPool, HttpServerAddress remoteAddress, final int maxMessageSize)
	{
		super(threadPool);
		this.remoteAddress = remoteAddress;
		start();
		setMaxMessageSize(maxMessageSize);
		connectProxyServer();
	}

	private synchronized SocketChannel connectProxyServer()
	{
		if(null != channel && channel.isConnected())
		{
			return channel;
		}
		
		if(logger.isDebugEnabled())
		{
			logger.debug("Connect remote proxy server.");
		}
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(maxMessageSize));
		pipeline.addLast("encoder", new HttpRequestEncoder());
		pipeline.addLast("handler", responseHandler);
		this.channel = factory.newChannel(pipeline);
		ChannelFuture future = channel.connect(new InetSocketAddress(remoteAddress.getHost(), remoteAddress.getPort())).awaitUninterruptibly();
		if(!future.isSuccess())
		{
			logger.error("Failed to connect proxy server." , future.getCause());
		}
		return channel;
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
		if(logger.isDebugEnabled())
		{
			logger.debug("send  data to:" + data.address.toPrintableString());
		}
		if(!channel.isConnected())
		{
			channel = connectProxyServer();
		}
		if(channel.isConnected())
		{
			HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, remoteAddress.getPath());
			request.setHeader("Host", remoteAddress.getHost() + ":" + remoteAddress.getPort());
			//
			request.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
			// request.setHeader(HttpHeaders.Names.TRANSFER_ENCODING,
			// HttpHeaders.Values.CHUNKED);
			request.setHeader(HttpHeaders.Names.CONTENT_TRANSFER_ENCODING, HttpHeaders.Values.BINARY);
			request.setHeader(HttpHeaders.Names.USER_AGENT, "hyk-proxy-client");
			request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/octet-stream");

			ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data.content.buffer());
			request.setHeader("Content-Length", String.valueOf(buffer.readableBytes()));

			request.setContent(buffer);
			channel.write(request).awaitUninterruptibly();
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
			//connectProxyServer();
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
					// response.getHeaderNames()
					logger.debug("Recv message with no body" + response.getHeaderNames());
					// response.g
				}
			}
		}
	}

}
