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
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.hyk.proxy.android.config.Config;
import org.hyk.proxy.android.config.Config.ConnectionMode;
import org.hyk.proxy.android.config.Config.ProxyInfo;
import org.hyk.proxy.android.config.Config.ProxyType;
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
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jivesoftware.smack.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.client.util.GoogleAvailableService;
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
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private List<RpcChannelData> recvList = new LinkedList<RpcChannelData>();

	private static Map<Channel, com.hyk.proxy.common.http.message.HttpServerAddress> addressTable = new ConcurrentHashMap<Channel, com.hyk.proxy.common.http.message.HttpServerAddress>();
	private static Map<Channel, Channel> waitingReplyChannelSet = new ConcurrentHashMap<Channel, Channel>();

	private static ClientSocketChannelFactory factory;

	private HttpClientSocketChannelSelector clientChannelSelector;

	private Map<Channel, LockAndCondition> syncLockTable = new ConcurrentHashMap<Channel, LockAndCondition>();

	private Config config;

	class LockAndCondition
	{
		public LockAndCondition(Lock lock, Condition cond)
		{
			this.lock = lock;
			this.cond = cond;
		}

		Lock lock;
		Condition cond;
		int responseCode = 0;
	}

	class HttpClientSocketChannel
	{
		SocketChannel socketChannel;
		HttpServerAddress remoteAddress;

		public HttpClientSocketChannel(HttpServerAddress remoteAddress)
		{
			this.socketChannel = null;
			this.remoteAddress = remoteAddress;
		}

		public synchronized SocketChannel getSocketChannel()
		{
			if (null == socketChannel || !socketChannel.isConnected())
			{
				socketChannel = connectProxyServer(remoteAddress);
				if (null == socketChannel)
				{
					// try again
					if (config.selectDefaultHttpProxy())
					{
						socketChannel = connectProxyServer(remoteAddress);
						if (null == socketChannel)
						{
							config.clearProxy();
							if (config.selectDefaultHttpsProxy())
							{
								socketChannel = connectProxyServer(remoteAddress);
							}
						}
					}
					else
					{
						//just try again
						socketChannel = connectProxyServer(remoteAddress);
					}
				}
			}
			return socketChannel;
			// return connectProxyServer();
		}

		public boolean isNotWaitingReply()
		{
			if (null == socketChannel || !socketChannel.isConnected()
			        || !waitingReplyChannelSet.containsKey(socketChannel))
			{
				if (null != socketChannel)
				{
					waitingReplyChannelSet.remove(socketChannel);
					return true;
				}
			}
			return false;
		}

		public void close()
		{
			if (socketChannel != null && socketChannel.isOpen())
			{
				socketChannel.close();
				socketChannel = null;
			}
		}
	}

	class HttpClientSocketChannelSelector
	{
		private Map<String, HttpClientSocketChannel[]> clientChannels = new HashMap<String, HttpClientSocketChannel[]>();
		//private int cursor;
		private final int maxHttpConnectionSizePerAppid;

		public HttpClientSocketChannelSelector(int maxHttpConnectionSizePerAppid)
		{
			super();
			this.maxHttpConnectionSizePerAppid = maxHttpConnectionSizePerAppid;
		}

		public synchronized HttpClientSocketChannel select(
		        HttpServerAddress remoteAddress)
		{
			HttpClientSocketChannel[] channels = clientChannels
			        .get(remoteAddress.getHost());
			if (null == channels)
			{
				channels = new HttpClientSocketChannel[maxHttpConnectionSizePerAppid];
				clientChannels.put(remoteAddress.getHost(), channels);
			}
			HttpClientSocketChannel channel = null;
			//int loopCount = 0;
			int start_cur = 0;
			while (null == channel
			        && start_cur < channels.length)
			{
				channel = channels[start_cur];
				if (null == channel)
				{
					channel = new HttpClientSocketChannel(remoteAddress);
					channels[start_cur] = channel;
					if(logger.isDebugEnabled())
					{
						logger.debug("Create " + start_cur + " channel for pool!");
					}
					break;
				}
				else if (!channel.isNotWaitingReply())
				{
					channel = null;
				}
				else
				{
					break;
				}
				start_cur++;
				//loopCount++;
			}
			if (null == channel)
			{
				channel = new HttpClientSocketChannel(remoteAddress);
			}
			else
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Select " + start_cur + " channel in pool!");
				}
			}
			return channel;
		}

		public void close()
		{
			for (HttpClientSocketChannel[] channels : clientChannels.values())
			{
				if (null != channels)
				{
					for (HttpClientSocketChannel channel : channels)
					{
						if (null != channel)
						{
							channel.close();
						}
					}
				}
			}
		}
	}

	private static void initClientSocketChannelFactory(Executor threadPool,
	        Config config)
	{
		if (null == factory)
		{
			String targetAddr = null;
			if (null != config.getHykProxyClientLocalProxy())
			{
				targetAddr = config.getHykProxyClientLocalProxy().host;
			}
			else
			{
				if (!config.getHykProxyServerAuths().isEmpty())
				{
					targetAddr = config.getHykProxyServerAuths().get(0).appid
					        + ".appspot.com";
				}
			}
			targetAddr = GoogleAvailableService.getInstance().getMappingHost(targetAddr);
			if (null == targetAddr || ClientUtils.isIPV6Address(targetAddr))
			{
				factory = new OioClientSocketChannelFactory(threadPool);
			}
			else
			{
				factory = new NioClientSocketChannelFactory(threadPool,
				        threadPool);
			}
		}
	}

	public HttpClientRpcChannel(Executor threadPool) throws IOException
	{
		super(threadPool);
		setMaxMessageSize(10240000);
		// this.remoteAddress = remoteAddress;
		this.config = Config.getInstance();
		// Java NIO is not support IPv6, here is a workaround
		initClientSocketChannelFactory(threadPool, config);
		start();
		// List<HttpClientSocketChannel> clientChannels = new
		// ArrayList<HttpClientSocketChannel>();
		int maxHttpConnectionSize = config.getHttpConnectionPoolSize();
		clientChannelSelector = new HttpClientSocketChannelSelector(
		        maxHttpConnectionSize);
	}

	private synchronized SocketChannel connectProxyServer(
	        HttpServerAddress remoteAddress)
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
		boolean sslProxyEnable = false;
		if (null != config.getHykProxyClientLocalProxy())
		{
			connectHost = config.getHykProxyClientLocalProxy().host;
			connectPort = config.getHykProxyClientLocalProxy().port;
			if (ProxyType.HTTPS
			        .equals(config.getHykProxyClientLocalProxy().type))
			{
				sslProxyEnable = true;
			}
			// except g.cn/google.cn
			if (null != config.getHykProxyClientLocalProxy().nextHopGoogleServer)
			{
				sslProxyEnable = true;
			}
		}
		else
		{
			connectHost = remoteAddress.getHost();
			connectPort = remoteAddress.getPort();
			sslProxyEnable = remoteAddress.isSecure();
		}
		connectHost = GoogleAvailableService.getInstance().getMappingHost(
		        connectHost);
		//if (logger.isDebugEnabled())
		{
			logger.info("Connect remote proxy server " + connectHost + ":"
			        + connectPort + " and sslProxyEnable:" + sslProxyEnable);
		}
		ChannelFuture future = channel.connect(
		        new InetSocketAddress(connectHost, connectPort))
		        .awaitUninterruptibly();
		if (!future.isSuccess())
		{
			logger.error("Failed to connect proxy server.", future.getCause());
			return null;
		}

		ProxyInfo info = config.getHykProxyClientLocalProxy();
		if (null != info && null != info.nextHopGoogleServer
		        && !info.host.contains("google."))
		{
			HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
			        HttpMethod.CONNECT, info.nextHopGoogleServer + ":443");
			request.setHeader(HttpHeaders.Names.HOST, info.nextHopGoogleServer
			        + ":443");
			if (null != info.user)
			{
				String userpass = info.user + ":" + info.passwd;
				String encode = Base64.encodeBytes(userpass.getBytes());
				request.setHeader(HttpHeaders.Names.PROXY_AUTHORIZATION,
				        "Basic " + encode);
			}
			try
			{
				ReentrantLock lock = new ReentrantLock();
				Condition cond = lock.newCondition();
				LockAndCondition lc = new LockAndCondition(lock, cond);
				syncLockTable.put(channel, lc);
				channel.write(request);
				try
				{
					lock.lock();
					if (cond.await(10, TimeUnit.SECONDS))
					{
						if (lc.responseCode == 200)
						{
							sslProxyEnable = true;
						}
						else
						{
							logger.error("Failed to send CONNECT to local proxy. Recv response code:"
							        + lc.responseCode);
							return null;
						}
					}
					else
					{
						logger.error("Timeout to send CONNECT to local proxy.");
						return null;
					}
				}
				finally
				{
					lock.unlock();
				}
			}
			catch (Exception e)
			{
				logger.error("Error occured!", e);
			}
			finally
			{
				syncLockTable.remove(channel);
			}
		}
		if (sslProxyEnable)
		{
			try
			{
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, null, null);
				SSLEngine sslEngine = sslContext.createSSLEngine(connectHost, connectPort);
				sslEngine.setUseClientMode(true);
				pipeline.addFirst("sslHandler", new SslHandler(sslEngine));
				ChannelFuture hf = channel.getPipeline().get(SslHandler.class)
				        .handshake(channel);
				hf.awaitUninterruptibly();
				if (!hf.isSuccess())
				{
					logger.error("Handshake failed", hf.getCause());
					channel.close();
					return null;
				}
				if (logger.isDebugEnabled())
				{
					logger.debug("SSL handshake success!");
				}
			}
			catch (Exception ex)
			{
				logger.error(null, ex);
				channel.close();
				return null;
			}
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
		synchronized (recvList)
		{
			if (recvList.isEmpty())
			{
				try
				{
					recvList.wait();
				}
				catch (InterruptedException e)
				{
					logger.error("", e);
					return null;
				}
			}
			return recvList.remove(0);
		}
	}

	@Override
	protected void send(RpcChannelData data) throws IOException
	{
		HttpServerAddress remoteAddress = (HttpServerAddress) data.address;
		if (config.getClient2ServerConnectionMode().equals(
		        ConnectionMode.HTTPS2GAE))
		{
			remoteAddress.trnasform2Https();
		}
		if (logger.isDebugEnabled())
		{
			logger.debug("send  data to:" + data.address.toPrintableString());
		}

		HttpClientSocketChannel clientChannel = clientChannelSelector
		        .select(remoteAddress);
		if(clientChannel.getSocketChannel() != null)
		{
			waitingReplyChannelSet.put(clientChannel.getSocketChannel(),
					clientChannel.getSocketChannel());
		}
		else
		{
			logger.error("Failed to get http(s) channel for sending RPC data.");
			return ;
		}
		String url = data.address.toPrintableString();
		// This option is only active when there is no local proxy or just an
		// anonymouse local proxy
		if (config.isSimpleURLEnable())
		{
			if (null == config.getHykProxyClientLocalProxy()
			        || null == config.getHykProxyClientLocalProxy().user)
			{
				url = remoteAddress.getPath();
			}
		}
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
		        HttpMethod.POST, url);
		request.setHeader("Host",
		        remoteAddress.getHost() + ":" + remoteAddress.getPort());
		request.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
		// request.setHeader(HttpHeaders.Names.CONNECTION, "close");
		if (null != config.getHykProxyClientLocalProxy())
		{
			ProxyInfo info = config.getHykProxyClientLocalProxy();
			if (null != info.user)
			{
				String userpass = info.user + ":" + info.passwd;
				String encode = Base64.encodeBytes(userpass.getBytes());
				request.setHeader(HttpHeaders.Names.PROXY_AUTHORIZATION,
				        "Basic " + encode);
			}
		}
		request.setHeader(HttpHeaders.Names.CONTENT_TRANSFER_ENCODING,
		        HttpHeaders.Values.BINARY);
		request.setHeader(HttpHeaders.Names.USER_AGENT, Config.getInstance()
		        .getSimulateUserAgent());
		// request.setHeader(HttpHeaders.Names.USER_AGENT, ");
		request.setHeader(HttpHeaders.Names.CONTENT_TYPE,
		        "application/octet-stream");

		RegistSecurityService reg = SecurityServiceFactory
		        .getRegistSecurityService(config.getHttpUpStreamEncrypter());
		ByteBuffer idbuf = ByteBuffer.allocate(4);
		idbuf.putInt(reg.id).flip();
		ByteBuffer[] bufs = reg.service.encrypt(ChannelDataBuffer
		        .asByteBuffers(data.content));
		ByteBuffer[] newbufs = new ByteBuffer[bufs.length + 1];
		newbufs[0] = idbuf;
		System.arraycopy(bufs, 0, newbufs, 1, bufs.length);
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(newbufs);
		request.setHeader("Content-Length",
		        String.valueOf(buffer.readableBytes()));
		request.setContent(buffer);

		ChannelFuture result = clientChannel.getSocketChannel().write(request)
		        .awaitUninterruptibly();
		if (logger.isDebugEnabled())
		{
			logger.debug("Send data to remote server " + remoteAddress);
		}
		// try again
		if (!result.isSuccess())
		{
			waitingReplyChannelSet.remove(clientChannel.getSocketChannel());
			clientChannel.close();
			clientChannel.getSocketChannel().write(request)
			        .awaitUninterruptibly();
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
		private volatile boolean readingChunks = false;
		// private ByteDataBuffer content;
		private ChannelBuffer content;

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
		        throws Exception
		{
			waitingReplyChannelSet.remove(e.getChannel());

			addressTable.remove(e.getChannel());
			e.getChannel().close();
			logger.error("exceptionCaught in HttpResponseHandler", e.getCause());
		}

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
		        throws Exception
		{
			addressTable.remove(e.getChannel());
			waitingReplyChannelSet.remove(e.getChannel());
			if (logger.isDebugEnabled())
			{
				logger.debug("Connection closed.");
			}
		}

		private void notifyRpcReader(Channel channel)
		{
			waitingReplyChannelSet.remove(channel);
			if(null == content)
			{
				logger.error("NULL content for RPC");
				return;
			}
			int secid = content.readInt();
			RegistSecurityService reg = SecurityServiceFactory
			        .getRegistSecurityService(secid);
			ByteBuffer data = content.toByteBuffer();

			if (null == reg)
			{
				logger.error("Can not decrypt data"
				        + new String(content.toByteBuffer().array()));
				return;
			}
			data = reg.service.decrypt(data);
			RpcChannelData recv = new RpcChannelData(ChannelDataBuffer.wrap(
			        data.array(), data.position(), data.remaining()),
			        addressTable.get(channel));
			synchronized (recvList)
			{
				recvList.add(recv);
				recvList.notify();
			}
			content = null;
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		        throws Exception
		{
			if (!readingChunks)
			{
				HttpResponse response = (HttpResponse) e.getMessage();
				int bodyLen = (int) response.getContentLength();
				if (logger.isDebugEnabled())
				{
					logger.debug("Recv message:" + response + " with len:"
					        + bodyLen + " from "
					        + addressTable.get(e.getChannel()));
				}

				if (syncLockTable.containsKey(e.getChannel()))
				{
					LockAndCondition lc = syncLockTable.get(e.getChannel());
					try
					{
						lc.lock.lock();
						lc.responseCode = response.getStatus().getCode();
						// workaround solution for netty 3.1.5
						HttpMessageDecoder decoder = e.getChannel()
						        .getPipeline().get(HttpResponseDecoder.class);
						Method m = HttpMessageDecoder.class.getDeclaredMethod(
						        "reset", null);
						m.setAccessible(true);
						m.invoke(decoder, null);

						lc.cond.signal();
					}
					finally
					{
						lc.lock.unlock();
					}
					return;
				}

				if (response.getStatus().getCode() == 200
				        && response.isChunked())
				{
					readingChunks = true;
				}
				else
				{
					if (response.getStatus().equals(HttpResponseStatus.OK)
					        && bodyLen > 0)
					{
						content = response.getContent();
						notifyRpcReader(e.getChannel());
					}
					else
					{
						if (logger.isDebugEnabled())
						{
							logger.debug("Recv message with no body or error rsponse"
							        + response);
						}
					}
				}
			}
			else
			{
				HttpChunk chunk = (HttpChunk) e.getMessage();
				if (chunk.isLast())
				{
					readingChunks = false;
					// content.
					notifyRpcReader(e.getChannel());
				}
				else
				{
					ChannelBuffer chunkContent = chunk.getContent();
					// ChannelBuffers.w
					// content.writeBytes(chunkContent);
					if (null == content)
					{
						content = chunkContent;
					}
					else
					{
						content = ChannelBuffers.wrappedBuffer(content,
						        chunkContent);
					}
				}
			}
		}
	}

}
