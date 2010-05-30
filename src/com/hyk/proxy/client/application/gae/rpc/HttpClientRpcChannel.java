/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpClientRpcChannel.java 
 *
 * @author yinqiwen [ Jan 28, 2010 | 5:41:08 PM ]
 *
 */
package com.hyk.proxy.client.application.gae.rpc;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
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

import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.proxy.client.application.gae.GoogleAppEngineApplicationConfig;
import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.config.Config.ProxyInfo;
import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.common.http.message.HttpServerAddress;
import com.hyk.proxy.common.secure.SecurityServiceFactory;
import com.hyk.proxy.common.secure.SecurityServiceFactory.RegistSecurityService;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.rpc.core.transport.impl.AbstractDefaultRpcChannel;

/**
 *
 */
public class HttpClientRpcChannel extends AbstractDefaultRpcChannel
{
	private List<RpcChannelData>				recvList	= new LinkedList<RpcChannelData>();

	private static Map<Channel, HttpServerAddress> addressTable = new ConcurrentHashMap<Channel, HttpServerAddress>();

	private static ClientSocketChannelFactory	factory;

	private HttpClientSocketChannelSelector		clientChannelSelector;

	private Config								config;

	class HttpClientSocketChannel
	{
		SocketChannel	socketChannel;
		HttpServerAddress remoteAddress;

		public HttpClientSocketChannel(HttpServerAddress remoteAddress)
		{
			this.socketChannel = null;
			this.remoteAddress = remoteAddress;
		}

		public synchronized SocketChannel getSocketChannel()
		{
			if(null == socketChannel || !socketChannel.isConnected())
			{
				socketChannel = connectProxyServer(remoteAddress);
				if(null == socketChannel)
				{
					// try again
					config.activateDefaultProxy();
					socketChannel = connectProxyServer(remoteAddress);
				}
			}
			return socketChannel;
			// return connectProxyServer();
		}

		public void close()
		{
			if(socketChannel != null && socketChannel.isOpen())
			{
				socketChannel.close();
				socketChannel = null;
			}
		}
	}

	class HttpClientSocketChannelSelector
	{
		private Map<String, HttpClientSocketChannel[]>	clientChannels = new HashMap<String, HttpClientSocketChannel[]>();
		private int								cursor;
		private final int maxHttpConnectionSizePerAppid;

		public HttpClientSocketChannelSelector(int maxHttpConnectionSizePerAppid)
		{
			super();
			this.maxHttpConnectionSizePerAppid = maxHttpConnectionSizePerAppid;
		}

		public synchronized HttpClientSocketChannel select(HttpServerAddress remoteAddress)
		{
			HttpClientSocketChannel[] channels = clientChannels.get(remoteAddress.getHost());
			if(null == channels)
			{
				channels = new HttpClientSocketChannel[maxHttpConnectionSizePerAppid];
				clientChannels.put(remoteAddress.getHost(), channels);
			}
			if(cursor >= channels.length)
			{
				cursor = 0;
			}
			HttpClientSocketChannel channel = channels[cursor];
			if(null == channel)
			{
				channel = new HttpClientSocketChannel(remoteAddress);
				channels[cursor] = channel;
			}
			cursor++;
			return channel;
		}

		public void close()
		{
			for(HttpClientSocketChannel[] channels : clientChannels.values())
			{
				if(null != channels)
				{
					for(HttpClientSocketChannel channel:channels)
					{
						if(null != channel)
						{
							channel.close();
						}
					}
				}
			}
		}
	}
	
	private static void initClientSocketChannelFactory(Executor threadPool, Config config)
	{
		if(null == factory)
		{
			String targetAddr = null;
			if(null != config.getHykProxyClientLocalProxy())
			{
				targetAddr = config.getHykProxyClientLocalProxy().host;
			}
			else
			{
				if(!config.getHykProxyServerAuths().isEmpty())
				{
					targetAddr = config.getHykProxyServerAuths().get(0).appid + ".appspot.com";
				}
			}
			if(null == targetAddr || ClientUtils.isIPV6Address(targetAddr))
			{
				factory = new OioClientSocketChannelFactory(threadPool);
			}
			else
			{
				factory = new NioClientSocketChannelFactory(threadPool, threadPool);
			}
		}
	}

	public HttpClientRpcChannel(Executor threadPool) throws IOException
	{
		super(threadPool);
		setMaxMessageSize(10240000);
		//this.remoteAddress = remoteAddress;
		this.config = Config.getInstance();
		// Java NIO is not support IPv6, here is a workaround
		initClientSocketChannelFactory(threadPool, config);
		start();
		//List<HttpClientSocketChannel> clientChannels = new ArrayList<HttpClientSocketChannel>();
		int maxHttpConnectionSize = config.getHttpConnectionPoolSize();
		clientChannelSelector = new HttpClientSocketChannelSelector(maxHttpConnectionSize);
	}

	private synchronized SocketChannel connectProxyServer(HttpServerAddress remoteAddress)
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
		if(null != config.getHykProxyClientLocalProxy())
		{
			connectHost = config.getHykProxyClientLocalProxy().host;
			connectPort = config.getHykProxyClientLocalProxy().port;
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
			return null;
		}
		addressTable.put(channel, remoteAddress);
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
	protected RpcChannelData recv() throws IOException
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
		HttpServerAddress remoteAddress = (HttpServerAddress)data.address;
		HttpClientSocketChannel clientChannel = clientChannelSelector.select(remoteAddress);
		String url = data.address.toPrintableString();
		// This option is only active when there is no local proxy or just an
		// anonymouse local proxy
		if(config.isSimpleURLEnable())
		{
			if(null == config.getHykProxyClientLocalProxy() || null == config.getHykProxyClientLocalProxy().user)
			{
				url = remoteAddress.getPath();
			}
		}
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, url);
		request.setHeader("Host", remoteAddress.getHost() + ":" + remoteAddress.getPort());
		request.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
		// request.setHeader(HttpHeaders.Names.CONNECTION, "close");
		if(null != config.getHykProxyClientLocalProxy())
		{
			ProxyInfo info = config.getHykProxyClientLocalProxy();
			if(null != info.user)
			{
				String userpass = info.user + ":" + info.passwd;
				String encode = Base64.encodeBytes(userpass.getBytes());
				request.setHeader(HttpHeaders.Names.PROXY_AUTHORIZATION, "Basic " + encode);
			}
		}
		request.setHeader(HttpHeaders.Names.CONTENT_TRANSFER_ENCODING, HttpHeaders.Values.BINARY);
		request.setHeader(HttpHeaders.Names.USER_AGENT, GoogleAppEngineApplicationConfig.getSimulateUserAgent());
		request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/octet-stream");

		RegistSecurityService reg = SecurityServiceFactory.getRegistSecurityService(config.getHttpUpStreamEncrypter());
		ByteBuffer idbuf = ByteBuffer.allocate(4);
		idbuf.putInt(reg.id).flip();
		ByteBuffer[] bufs = reg.service.encrypt(ChannelDataBuffer.asByteBuffers(data.content));
		ByteBuffer[] newbufs = new ByteBuffer[bufs.length + 1];
		newbufs[0] = idbuf;
		System.arraycopy(bufs, 0, newbufs, 1, bufs.length);
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(newbufs);
		request.setHeader("Content-Length", String.valueOf(buffer.readableBytes()));

		request.setContent(buffer);
		ChannelFuture result = clientChannel.getSocketChannel().write(request).awaitUninterruptibly();
		if(logger.isDebugEnabled())
		{
			logger.debug("Send data to remote server " + remoteAddress);
		}
		//try again
		if(!result.isSuccess())
		{
			clientChannel.close();
			clientChannel.getSocketChannel().write(request).awaitUninterruptibly();
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
		private volatile boolean	readingChunks	= false;
		// private ByteDataBuffer content;
		private ChannelBuffer		content;
		
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
		{
			if(e.getCause() instanceof ConnectException)
			{
				config.activateDefaultProxy();
			}
			if(e.getCause() instanceof IOException)
			{
				e.getChannel().close();
			}
			super.exceptionCaught(ctx, e);
		}

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
		{
			addressTable.remove(e.getChannel());
			if(logger.isDebugEnabled())
			{
				logger.debug("Connection closed.");
			}
		}

		private void notifyRpcReader(Channel channel)
		{
			int secid = content.readInt();
			RegistSecurityService reg = SecurityServiceFactory.getRegistSecurityService(secid);
			ByteBuffer data = content.toByteBuffer();
			data = reg.service.decrypt(data);

			RpcChannelData recv = new RpcChannelData(ChannelDataBuffer.wrap(data.array(), data.position(), data.remaining()), addressTable.get(channel));
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
					logger.debug("Recv message:" + response + " with len:" + bodyLen + " from " + addressTable.get(e.getChannel()));
				}

				if(response.getStatus().getCode() == 200 && response.isChunked())
				{
					readingChunks = true;
					content = ChannelBuffers.buffer(bodyLen);
				}
				else
				{
					if(response.getStatus().equals(HttpResponseStatus.OK) && bodyLen > 0)
					{
						content = response.getContent();
						notifyRpcReader(e.getChannel());
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
					// content.
					notifyRpcReader(e.getChannel());
				}
				else
				{
					ChannelBuffer chunkContent = chunk.getContent();
					content.writeBytes(chunkContent);
				}
			}
		}
	}

}
