/**
 * 
 */
package org.hyk.proxy.gae.client.connection;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.arch.buffer.Buffer;
import org.arch.misc.crypto.base64.Base64;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.ConnectionMode;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.GAEServerAuth;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.ProxyInfo;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.ProxyType;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.http.HttpServerAddress;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.SocketChannel;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 * 
 */
public class HTTPProxyConnection extends ProxyConnection
{
	private static final int INITED = 0;
	private static final int WAITING_CONNECT_RESPONSE = 1;
	private static final int CONNECT_RESPONSED = 2;
	private static final int DISCONNECTED = 3;

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private static ClientSocketChannelFactory factory;

	private boolean waitingResponse = false;
	private SocketChannel clientChannel = null;
	
	private HttpServerAddress remoteAddress = null;
	private AtomicInteger sslProxyConnectionStatus = new AtomicInteger(0);

	public HTTPProxyConnection(GAEServerAuth auth)
	{
		// remoteAddress = address;
		remoteAddress = new HttpServerAddress(auth.appid + ".appspot.com",
		        GAEConstants.HTTP_INVOKE_PATH, GAEClientConfiguration
		                .getInstance().getConnectionMode()
		                .equals(ConnectionMode.HTTPS));
		this.auth = auth;
	}

	public boolean isReady()
	{
		return !waitingResponse;
	}

	@Override
	protected int getMaxDataPackageSize()
	{
		return GAEConstants.APPENGINE_HTTP_BODY_LIMIT;
	}

	protected SocketChannel getHTTPClientChannel()
	{
		if (null == clientChannel || !clientChannel.isConnected())
		{
			clientChannel = connectProxyServer(remoteAddress);
			if (!clientChannel.isConnected())
			{
				return null;
			}
		}
		return clientChannel;
	}

	private synchronized SocketChannel connectProxyServer(
	        HttpServerAddress address)
	{
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new HttpResponseDecoder());
		// pipeline.addLast("aggregator", new
		// HttpChunkAggregator(maxMessageSize));
		pipeline.addLast("encoder", new HttpRequestEncoder());
		pipeline.addLast("handler", new HttpResponseHandler());
		SocketChannel channel = factory.newChannel(pipeline);
		String connectHost;
		int connectPort;
		boolean sslProxyEnable = false;
		if (null != cfg.getLocalProxy())
		{
			connectHost = cfg.getLocalProxy().host;
			connectPort = cfg.getLocalProxy().port;
			if (ProxyType.HTTPS.equals(cfg.getLocalProxy().type))
			{
				sslProxyEnable = true;
			}
			// except g.cn/google.cn
			if (null != cfg.getLocalProxy().nextHopGoogleServer)
			{
				sslProxyEnable = true;
			}
		}
		else
		{
			connectHost = address.getHost();
			connectPort = address.getPort();
			sslProxyEnable = address.isSecure();
		}
		connectHost = cfg.getMappingHost(connectHost);
		if (logger.isInfoEnabled())
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

		ProxyInfo info = cfg.getLocalProxy();
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
				String encode = Base64.encodeToString(userpass.getBytes(),
				        false);
				request.setHeader(HttpHeaders.Names.PROXY_AUTHORIZATION,
				        "Basic " + encode);
			}
			sslProxyConnectionStatus.set(WAITING_CONNECT_RESPONSE);
			channel.write(request);
			synchronized (sslProxyConnectionStatus)
			{
				try
				{
					sslProxyConnectionStatus.wait();
					if (sslProxyConnectionStatus.get() != CONNECT_RESPONSED)
					{
						return null;
					}
				}
				catch (InterruptedException e)
				{

				}
				finally
				{
					sslProxyConnectionStatus.set(INITED);
				}
			}
		}

		if (sslProxyEnable)
		{
			try
			{
				SSLContext sslContext = SSLContext.getDefault();
				SSLEngine sslEngine = sslContext.createSSLEngine();
				sslEngine.setUseClientMode(true);
				pipeline.addFirst("sslHandler", new SslHandler(sslEngine));
				ChannelFuture hf = channel.getPipeline().get(SslHandler.class)
				        .handshake();
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
		return channel;
	}

	protected boolean doSend(Buffer content)
	{
		if (cfg.getConnectionMode().equals(ConnectionMode.HTTPS))
		{
			remoteAddress.trnasform2Https();
		}
		getHTTPClientChannel();
		if (clientChannel == null)
		{
			return false;
		}
		String url = remoteAddress.toPrintableString();
		// This option is only active when there is no local proxy or just an
		// anonymouse local proxy
		if (cfg.isSimpleURLEnable())
		{
			if (null == cfg.getLocalProxy() || null == cfg.getLocalProxy().user)
			{
				url = remoteAddress.getPath();
			}
		}
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
		        HttpMethod.POST, url);
		request.setHeader("Host",
		        remoteAddress.getHost() + ":" + remoteAddress.getPort());
		request.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
		if (null != cfg.getLocalProxy())
		{
			ProxyInfo info = cfg.getLocalProxy();
			if (null != info.user)
			{
				String userpass = info.user + ":" + info.passwd;
				String encode = Base64.encodeToString(userpass.getBytes(),
				        false);
				request.setHeader(HttpHeaders.Names.PROXY_AUTHORIZATION,
				        "Basic " + encode);
			}
		}
		request.setHeader(HttpHeaders.Names.CONTENT_TRANSFER_ENCODING,
		        HttpHeaders.Values.BINARY);
		request.setHeader(HttpHeaders.Names.USER_AGENT, cfg.getUserAgent());
		request.setHeader(HttpHeaders.Names.CONTENT_TYPE,
		        "application/octet-stream");

		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(
		        content.getRawBuffer(), content.getReadIndex(),
		        content.readableBytes());
		request.setHeader("Content-Length",
		        String.valueOf(buffer.readableBytes()));
		request.setContent(buffer);
		waitingResponse = true;
		clientChannel.write(request);
		return true;
	}

	private void updateSSLProxyConnectionStatus(int status)
	{
		synchronized (sslProxyConnectionStatus)
		{
			sslProxyConnectionStatus.set(status);
			sslProxyConnectionStatus.notify();
		}
	}

	private boolean casSSLProxyConnectionStatus(int current, int status)
	{
		synchronized (sslProxyConnectionStatus)
		{
			int cur = sslProxyConnectionStatus.get();
			if (cur != current)
			{
				return false;
			}
			sslProxyConnectionStatus.set(status);
			sslProxyConnectionStatus.notify();
			return true;
		}
	}

	class HttpResponseHandler extends SimpleChannelUpstreamHandler
	{
		private boolean readingChunks = false;
		private int responseContentLength = 0;
		private Buffer resBuffer = new Buffer(0);

		private void fillResponseBuffer(ChannelBuffer buffer)
		{
			int contentlen = buffer.readableBytes();
			resBuffer.ensureWritableBytes(contentlen);
			buffer.readBytes(resBuffer.getRawBuffer(), resBuffer.getWriteIndex(),contentlen);
			resBuffer.advanceWriteIndex(contentlen);
			if(responseContentLength <= resBuffer.readableBytes())
			{
				doRecv(resBuffer);
				resBuffer.clear();
			}
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
		        throws Exception
		{
			e.getChannel().close();
			logger.error("exceptionCaught in HttpResponseHandler", e.getCause());
			updateSSLProxyConnectionStatus(DISCONNECTED);
			waitingResponse = false;
		}

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
		        throws Exception
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Connection closed.");
			}
			updateSSLProxyConnectionStatus(DISCONNECTED);
			waitingResponse = false;
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		        throws Exception
		{
			waitingResponse = false;
			if (!readingChunks)
			{
				HttpResponse response = (HttpResponse) e.getMessage();

				// workaround solution for netty 3.1.5
				if (casSSLProxyConnectionStatus(WAITING_CONNECT_RESPONSE,
				        CONNECT_RESPONSED))
				{
					HttpMessageDecoder decoder = e.getChannel().getPipeline()
					        .get(HttpResponseDecoder.class);
					Method m = HttpMessageDecoder.class.getDeclaredMethod(
					        "reset", null);
					m.setAccessible(true);
					m.invoke(decoder, null);
					return;
				}

				responseContentLength = (int) HttpHeaders.getContentLength(response);
				if (response.getStatus().getCode() == 200)
				{
					if(response.isChunked())
					{
						readingChunks = true;
						waitingResponse = true;
					}
					else
					{
						readingChunks = false;
						waitingResponse = false;
					}
					ChannelBuffer content = response.getContent();
					fillResponseBuffer(content);
				}
				else
				{
					waitingResponse = false;       
				}
			}
			else
			{
				HttpChunk chunk = (HttpChunk) e.getMessage();
				if (chunk.isLast())
				{
					readingChunks = false;
					waitingResponse = false;
				}
				fillResponseBuffer(chunk.getContent());
			}
		}
	}

}
