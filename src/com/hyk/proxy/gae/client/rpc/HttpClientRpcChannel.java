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
import java.net.ConnectException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
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
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.SocketChannel;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jivesoftware.smack.util.Base64;

import com.hyk.proxy.gae.client.config.Config;
import com.hyk.proxy.gae.client.config.ProxyInfo;
import com.hyk.proxy.gae.client.util.ClientUtils;
import com.hyk.proxy.gae.common.http.message.HttpServerAddress;
import com.hyk.rpc.core.transport.AbstractDefaultRpcChannel;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.util.buffer.ByteArray;

/**
 *
 */
public class HttpClientRpcChannel extends AbstractDefaultRpcChannel
{
	private List<RpcChannelData>			recvList		= new LinkedList<RpcChannelData>();

	private HttpServerAddress				remoteAddress;

	private ClientSocketChannelFactory		factory;

	private HttpClientSocketChannelSelector	clientChannelSelector;

	private Config							config;

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
			//return connectProxyServer();
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

	public HttpClientRpcChannel(Executor threadPool, HttpServerAddress remoteAddress) throws IOException
	{
		super(threadPool);
		setMaxMessageSize(10240000);
		this.remoteAddress = remoteAddress;
		// Java NIO is not support IPv6, here is a workaround
		if(ClientUtils.isIPV6Address(remoteAddress.getHost()))
		{
			factory = new OioClientSocketChannelFactory(threadPool);
		}
		else
		{
			factory = new NioClientSocketChannelFactory(threadPool, threadPool);
		}
		start();
		List<HttpClientSocketChannel> clientChannels = new ArrayList<HttpClientSocketChannel>();
		int maxHttpConnectionSize = Config.getInstance().getHttpConnectionPoolSize();
		for(int i = 0; i < maxHttpConnectionSize; i++)
		{
			clientChannels.add(new HttpClientSocketChannel());
		}
		clientChannelSelector = new HttpClientSocketChannelSelector(clientChannels);
		config = Config.getInstance();
	}

	private synchronized SocketChannel connectProxyServer()
	{
		
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("decoder", new HttpResponseDecoder());
		// pipeline.addLast("aggregator", new
		// HttpChunkAggregator(maxMessageSize));
		pipeline.addLast("encoder", new HttpRequestEncoder());
		pipeline.addLast("handler", new HttpResponseHandler());
		SocketChannel channel = factory.newChannel(pipeline);
		String connectHost;
		int connectPort;
		if(null != config.getProxyInfo())
		{
			connectHost = config.getProxyInfo().getHost();
			connectPort = config.getProxyInfo().getPort();
		}
		else
		{
			connectHost = remoteAddress.getHost();
			connectPort = remoteAddress.getPort();
		}
		if(logger.isDebugEnabled())
		{
			logger.debug("Connect remote proxy server " + connectHost + ":" + connectPort);
		}
		ChannelFuture future = channel.connect(new InetSocketAddress(connectHost, connectPort)).awaitUninterruptibly();
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
			String url = remoteAddress.toPrintableString();
			//This option is only active when there is no local proxy or just an anonymouse local proxy
			if(config.isSimpleUrlEnable())
			{
				if(null ==  config.getProxyInfo() ||  null == config.getProxyInfo().getUser())
				{
					url = remoteAddress.getPath();
				}
			}
			HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, url);
			request.setHeader("Host", remoteAddress.getHost() + ":" + remoteAddress.getPort());
			request.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
			//request.setHeader(HttpHeaders.Names.CONNECTION, "close");
			if(null != config.getProxyInfo())
			{
				ProxyInfo info = config.getProxyInfo();
				if(null != info.getUser())
				{
					String userpass = info.getUser() + ":" + info.getPassword();
					String encode = Base64.encodeBytes(userpass.getBytes());
					request.setHeader(HttpHeaders.Names.PROXY_AUTHORIZATION, "Basic " + encode);
				}
			}
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

	@ChannelPipelineCoverage("one")
	class HttpResponseHandler extends SimpleChannelUpstreamHandler
	{
		private volatile boolean	readingChunks = false;
		private ByteArray content;

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
		{
			if(e.getCause() instanceof ConnectException)
			{
				config.activateDefaultProxy();
			}
			super.exceptionCaught(ctx, e);
		}
		
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Connection closed.");
			}
		}
		
		private void notifyRpcReader()
		{
			RpcChannelData recv = new RpcChannelData(content, remoteAddress);
			synchronized(recvList)
			{
				recvList.add(recv);
				recvList.notify();
			}
			content = null;
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
		{
			if(!readingChunks)
			{
				HttpResponse response = (HttpResponse)e.getMessage();

				int bodyLen = (int)response.getContentLength();
				if(logger.isDebugEnabled())
				{
					logger.debug("Recv message:" + response + " with len:" + bodyLen);
				}
				content = ByteArray.allocate(bodyLen);
				if(response.getStatus().getCode() == 200 && response.isChunked())
				{
					readingChunks = true;
				}
				else
				{
					//content = response.getContent();
					if(response.getStatus().equals(HttpResponseStatus.OK) && bodyLen > 0)
					{
						// response.g
						ChannelBuffer body = response.getContent();
						body.readBytes(content.rawbuffer(), 0, bodyLen);
						content.position(bodyLen);
						content.flip();
						notifyRpcReader();
					}
					else
					{
						if(logger.isDebugEnabled())
						{
							logger.debug("Recv message with no body or error rsponse" + response);
						}
					}
				}	
			}
			else
			{
				HttpChunk chunk = (HttpChunk)e.getMessage();
				if(chunk.isLast())
				{
					readingChunks = false;
					content.rewind();
					notifyRpcReader();
				}
				else
				{
					ChannelBuffer chunkContent = chunk.getContent();
					//chunkContent.read
					byte[] rawbuf = content.rawbuffer();
					int offset = content.position();
					int len = chunkContent.readableBytes();
					chunkContent.readBytes(rawbuf, offset, len);
					content.position(offset + len);
				}
			}
		}
	}

}
