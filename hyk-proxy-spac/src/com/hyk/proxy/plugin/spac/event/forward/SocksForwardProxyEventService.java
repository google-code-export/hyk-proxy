/**
 * This file is part of the hyk-proxy-spac project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SocksForwardProxyEventService.java 
 *
 * @author yinqiwen [ 2011-5-12 | ÏÂÎç08:31:18 ]
 *
 */
package com.hyk.proxy.plugin.spac.event.forward;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.jsocks.socks.Proxy;
import net.sourceforge.jsocks.socks.Socks4Proxy;
import net.sourceforge.jsocks.socks.Socks5Proxy;
import net.sourceforge.jsocks.socks.SocksException;
import net.sourceforge.jsocks.socks.SocksSocket;

import com.hyk.proxy.framework.common.Misc;
import com.hyk.proxy.framework.event.HttpProxyEvent;
import com.hyk.proxy.framework.event.HttpProxyEventCallback;
import com.hyk.proxy.framework.event.HttpProxyEventService;
import com.hyk.proxy.framework.event.HttpProxyEventType;
import com.hyk.proxy.framework.event.tunnel.AbstractTunnelProxyEventService;

/**
 *
 */
public class SocksForwardProxyEventService implements HttpProxyEventService,
        Runnable
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private SocksSocket client;

	private Proxy socksproxy;

	protected Channel localChannel;

	private HttpProxyEventCallback callback;

	public void setSocksProxy(String protocol, String host, int port)
	        throws UnknownHostException
	{
		if (protocol.equalsIgnoreCase("socks5"))
		{
			socksproxy = new Socks5Proxy(host, port);
			((Socks5Proxy) socksproxy).resolveAddrLocally(false);
		}
		else if (protocol.equalsIgnoreCase("socks4"))
		{
			socksproxy = new Socks4Proxy(host, port, "");
		}
		else
		{
			throw new UnknownHostException("Invalid protocol");
		}
	}

	private void closeLocalChannel()
	{
		if (null != localChannel && localChannel.isOpen())
		{
			localChannel.close();
		}
		localChannel = null;
	}

	private void closeSocksClient()
	{
		if (null != client)
		{
			try
			{
				client.close();
			}
			catch (IOException e)
			{
				logger.error("Failed to close socks client.", e);
			}
			client = null;
		}
	}

	protected InetSocketAddress getHostHeaderAddress(HttpRequest request)
	{
		String host = request.getHeader("Host");
		if (null == host)
		{
			String url = request.getUri();
			if (url.startsWith("http://"))
			{
				url = url.substring(7);
				int next = url.indexOf("/");
				host = url.substring(0, next);
			}
			else
			{
				host = url;
			}
		}
		int index = host.indexOf(":");
		int port = 80;
		if (request.getMethod().equals(HttpMethod.CONNECT))
		{
			port = 443;
		}
		String hostValue = host;
		if (index > 0)
		{
			hostValue = host.substring(0, index).trim();
			port = Integer.parseInt(host.substring(index + 1).trim());
		}
		if (logger.isDebugEnabled())
		{
			logger.debug("Get remote address " + host + ":" + port);
		}
		return new InetSocketAddress(hostValue, port);
	}

	private void writeRemoteSocket(ChannelBuffer buffer) throws IOException
	{
		if (null != client)
		{
			byte[] tmp = new byte[buffer.readableBytes()];
			buffer.getBytes(buffer.readerIndex(), tmp);
			client.getOutputStream().write(tmp);
			client.getOutputStream().flush();
		}

	}

	protected void removeCodecHandler(final Channel channel,
	        ChannelFuture future)
	{
		if (null != future)
		{
			future.addListener(new ChannelFutureListener()
			{

				@Override
				public void operationComplete(ChannelFuture future)
				        throws Exception
				{
					channel.getPipeline().remove("decoder");
					channel.getPipeline().remove("encoder");
					channel.getPipeline().addLast("empty",
					        new AbstractTunnelProxyEventService.EmptyHandler());
				}

			});
		}
		else
		{
			channel.getPipeline().remove("decoder");
			channel.getPipeline().remove("encoder");
			channel.getPipeline().addLast("empty",
			        new AbstractTunnelProxyEventService.EmptyHandler());
		}

	}

	@Override
	public void handleEvent(HttpProxyEvent event,
	        HttpProxyEventCallback callback)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("Handle event:" + event.getType());
		}
		this.callback = callback;
		switch (event.getType())
		{
			case RECV_HTTP_REQUEST:
			case RECV_HTTPS_REQUEST:
			{
				localChannel = event.getChannel();
				HttpRequest request = (HttpRequest) event.getSource();
				InetSocketAddress remote = getHostHeaderAddress(request);
				try
				{
					if (null == client)
					{
						client = new SocksSocket(socksproxy,
						        remote.getHostName(), remote.getPort());
						if (logger.isDebugEnabled())
						{
							logger.debug("Create a socks socket fore remote:"
							        + remote);
						}
						Misc.getGlobalThreadPool().submit(this);
					}
					if (request.getMethod().equals(HttpMethod.CONNECT))
					{
						HttpResponse res = new DefaultHttpResponse(
						        HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
						ChannelFuture future = event.getChannel().write(res);
						removeCodecHandler(event.getChannel(), future);
						future.awaitUninterruptibly();
						return;
					}
					else
					{
						// to bytes
						String uri = request.getUri();
						HttpRequest newreq = request;
						if (request.getHeader(HttpHeaders.Names.HOST) != null)
						{
							String prefix = "http://"
							        + request.getHeader(HttpHeaders.Names.HOST);
							if (uri.startsWith(prefix))
							{
								// truncate prefix
								uri = uri.substring(prefix.length());
								newreq = new DefaultHttpRequest(
								        request.getProtocolVersion(),
								        request.getMethod(), uri);
								Set<String> headers = request.getHeaderNames();
								for (String headerName : headers)
								{
									List<String> headerValues = request
									        .getHeaders(headerName);
									if (null != headerValues)
									{
										for (String headerValue : headerValues)
										{
											newreq.addHeader(headerName,
											        headerValue);
										}
									}
								}
								newreq.setContent(request.getContent());

							}
						}
						// removeCodecHandler(event.getChannel(), null);
						// request.removeHeader("Proxy-Connection");
						// request.removeHeader("Cookie");

						// Field f =
						// DefaultHttpRequest.class.getDeclaredField("uri");
						// f.setAccessible(true);
						// String url = request.getUri();
						// if(url.startsWith("http://" +
						// request.getHeader("Host")))
						// {
						// url =
						// url.substring(request.getHeader("Host").length() +
						// 7);
						// }
						// f.set(request, url);
						// HttpRequest newreq = new
						// DefaultHttpRequest(request.getProtocolVersion(),
						// request.getMethod(), url);
						HttpRequestEncoder encoder = new HttpRequestEncoder();
						Method m = HttpMessageEncoder.class.getDeclaredMethod(
						        "encode", ChannelHandlerContext.class,
						        Channel.class, Object.class);
						m.setAccessible(true);
						ChannelBuffer buf = (ChannelBuffer) m.invoke(encoder,
						        null, event.getChannel(), newreq);
						writeRemoteSocket(buf);

					}
				}
				catch (Exception e)
				{
					logger.error("Failed to write remote channel!", e);
					closeLocalChannel();
					closeSocksClient();
				}
				break;
			}
			case RECV_HTTP_CHUNK:
			case RECV_HTTPS_CHUNK:
			{
				ChannelBuffer buffer = null;

				if (event.getSource() instanceof HttpChunk)
				{
					HttpChunk chunk = (HttpChunk) event.getSource();
					buffer = chunk.getContent();
				}
				else if (event.getSource() instanceof ChannelBuffer)
				{
					buffer = (ChannelBuffer) event.getSource();
				}
				else
				{
					logger.error("Unexpected event type:"
					        + event.getSource().getClass());
					closeLocalChannel();
					closeSocksClient();
					return;
				}
				try
				{
					if (null != buffer)
					{
						if (logger.isDebugEnabled())
						{
							logger.debug("Write " + buffer.readableBytes()
							        + " bytes to socks proxy!");
						}
						// buffer.getBytes(0, client.getOutputStream(),
						// buffer.capacity());
						// client.getChannel().write(buffer.toByteBuffer());
						writeRemoteSocket(buffer);
					}

				}
				catch (IOException e)
				{
					logger.error("Failed to write remote channel!", e);
					closeLocalChannel();
					closeSocksClient();
				}
				// client.getOutputStream().write(b);
				break;
			}
		}

	}

	@Override
	public void close() throws Exception
	{
		logger.error("Close this handler.");
		closeLocalChannel();
		closeSocksClient();

	}

	@Override
	public void run()
	{
		byte[] buf = new byte[8192];
		while (true)
		{
			try
			{
				int ret = client.getInputStream().read(buf);
				if (ret > 0)
				{
					localChannel
					        .write(ChannelBuffers.copiedBuffer(buf, 0, ret))
					        .awaitUninterruptibly();

				}
				else
				{
					logger.error("Recv none bytes:" + ret);
					// break;
					if (ret < 0)
					{
						break;
					}
				}
			}
			catch (IOException e)
			{
				logger.error("Failed to read socks client.", e);
				break;
			}
		}
		closeLocalChannel();
		closeSocksClient();
	}

	public static void main(String[] args)
	{
		Method[] ms = HttpMessageEncoder.class.getDeclaredMethods();
		for (Method m : ms)
		{
			System.out.println(m.toString());
		}
	}
}
