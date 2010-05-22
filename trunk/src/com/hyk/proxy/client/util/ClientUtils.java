/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ClientUtils.java 
 *
 * @author yinqiwen [ 2010-2-1 | 10:11:39 PM ]
 *
 */
package com.hyk.proxy.client.util;

import java.io.Console;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.compress.CompressorFactory;
import com.hyk.compress.preference.DefaultCompressPreference;
import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.client.config.Config.ConnectionMode;
import com.hyk.proxy.client.config.Config.XmppAccount;
import com.hyk.proxy.client.application.gae.rpc.HttpClientRpcChannel;
import com.hyk.proxy.client.application.gae.rpc.XmppRpcChannel; //import com.hyk.proxy.client.httpserver.HttpServer;
//import com.hyk.proxy.client.rpc.HttpClientRpcChannel;
//import com.hyk.proxy.client.rpc.XmppRpcChannel;
import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.http.header.SetCookieHeaderValue;
import com.hyk.proxy.common.http.message.HttpResponseExchange;
import com.hyk.proxy.common.http.message.HttpServerAddress;
import com.hyk.proxy.common.rpc.service.MasterNodeService;
import com.hyk.proxy.common.xmpp.XmppAddress;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.RpcException;
import com.hyk.rpc.core.constant.RpcConstants;

/**
 *
 */
public class ClientUtils
{
	protected static Logger				logger					= LoggerFactory.getLogger(ClientUtils.class);

	private static byte[]				STDIN_BUFFER			= new byte[1024];
	private static Console				console					= System.console();

	private static final String			ContentRangeValueHeader	= "bytes";
	private static MasterNodeService	master					= null;

	public static SSLContext initSSLContext() throws Exception
	{
		String password = "hyk-proxy";
		SSLContext sslContext = SSLContext.getInstance("TLS");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(ClientUtils.class.getResourceAsStream("/hyk-proxy.cert"), password.toCharArray());
		kmf.init(ks, password.toCharArray());
		KeyManager[] km = kmf.getKeyManagers();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		TrustManager[] tm = tmf.getTrustManagers();
		sslContext.init(km, tm, null);
		return sslContext;
	}

	public static boolean isIPV6Address(String address)
	{
		try
		{
			return InetAddress.getByName(address) instanceof Inet6Address;
		}
		catch(Throwable e)
		{
			return false;
		}
	}

	public static HttpResponse buildHttpServletResponse(HttpResponseExchange forwardResponse) throws IOException
	{

		if(null == forwardResponse)
		{
			return new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT);
		}
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(forwardResponse.getResponseCode()));

		List<String[]> headers = forwardResponse.getHeaders();
		for(String[] header : headers)
		{
			if(header[0].equalsIgnoreCase(HttpHeaders.Names.SET_COOKIE) || header[0].equalsIgnoreCase(HttpHeaders.Names.SET_COOKIE2))
			{
				List<SetCookieHeaderValue> cookies = SetCookieHeaderValue.parse(header[1]);
				for(SetCookieHeaderValue cookie : cookies)
				{
					response.addHeader(header[0], cookie.toString());
				}
			}
			else
			{
				response.addHeader(header[0], header[1]);
			}
		}
		byte[] content = forwardResponse.getBody();
		if(null != content)
		{
			ChannelBuffer bufer = ChannelBuffers.wrappedBuffer(content);
			response.setContent(bufer);
		}

		return response;
	}

	public static long[] parseContentRange(String value)
	{
		String left = value.substring(ContentRangeValueHeader.length()).trim();
		String[] split = left.split("/");
		String[] split2 = split[0].split("-");
		long[] ret = new long[3];
		ret[0] = Long.parseLong(split2[0].trim());
		ret[1] = Long.parseLong(split2[1].trim());
		ret[2] = Long.parseLong(split[1].trim());
		return ret;
	}

	public static boolean isCompleteResponse(HttpResponseExchange response)
	{
		String contentRange = response.getHeaderValue(HttpHeaders.Names.CONTENT_RANGE);
		if(null == contentRange)
		{
			return true;
		}
		long[] lens = parseContentRange(contentRange);
		if(lens[1] >= (lens[2] - 1))
		{
			return true;
		}
		return false;
	}

	public static HttpServerAddress createHttpServerAddress(String appid)
	{
		return new HttpServerAddress(appid + ".appspot.com", Constants.HTTP_INVOKE_PATH);
	}

	public static XmppAddress createXmppAddress(String appid)
	{
		return new XmppAddress(appid + "@appspot.com");
	}

	public static MasterNodeService getMasterNodeService(Config config) throws IOException, RpcException, XMPPException
	{
		if(null != master)
		{
			return master;
		}
		RPC rpc = null;
		if(config.getClient2ServerConnectionMode().equals(ConnectionMode.HTTP2GAE))
		{
			rpc = ClientUtils.createHttpRPC(Constants.MASTER_APPID, Executors.newCachedThreadPool());
			master = rpc.getRemoteService(MasterNodeService.class, MasterNodeService.NAME,
					ClientUtils.createHttpServerAddress(Constants.MASTER_APPID));

		}
		else
		{
			if(null != config.getXmppAccounts())
			{
				XmppAccount account = config.getXmppAccounts().get(0);
				rpc = ClientUtils.createXmppRPC(account, Executors.newCachedThreadPool());
				master = rpc.getRemoteService(MasterNodeService.class, MasterNodeService.NAME, ClientUtils.createXmppAddress(Constants.MASTER_APPID));
			}
		}
		return master;
	}

	public static RPC createHttpRPC(String appid, Executor workerExecutor) throws IOException, RpcException
	{
		Config config = Config.getInstance();
		DefaultCompressPreference.init(CompressorFactory.getRegistCompressor(config.getCompressor()).compressor);
		Properties initProps = new Properties();
		initProps.setProperty(RpcConstants.SESSIN_TIMEOUT, Integer.toString(config.getRpcTimeOut() * 1000));
		initProps.setProperty(RpcConstants.COMPRESS_PREFER, DefaultCompressPreference.class.getName());
		HttpServerAddress remoteAddress = new HttpServerAddress(appid + ".appspot.com", Constants.HTTP_INVOKE_PATH);
		// HttpServerAddress remoteAddress = new HttpServerAddress("localhost",
		// 8888, Constants.HTTP_INVOKE_PATH);
		HttpClientRpcChannel httpCleintRpcchannle = new HttpClientRpcChannel(workerExecutor, remoteAddress);
		return new RPC(httpCleintRpcchannle, initProps);
	}

	public static RPC createXmppRPC(XmppAccount account, Executor workerExecutor) throws IOException, RpcException, XMPPException
	{
		Config config = Config.getInstance();
		DefaultCompressPreference.init(CompressorFactory.getRegistCompressor(config.getCompressor()).compressor);
		Properties initProps = new Properties();
		initProps.setProperty(RpcConstants.SESSIN_TIMEOUT, Integer.toString(config.getRpcTimeOut() * 1000));
		initProps.setProperty(RpcConstants.COMPRESS_PREFER, DefaultCompressPreference.class.getName());
		XmppRpcChannel xmppRpcchannle = new XmppRpcChannel(workerExecutor, account);
		return new RPC(xmppRpcchannle, initProps);
	}

	public static boolean isHTTPServerReachable(String appid)
	{
		String server = appid + ".appspot.com";
		Socket socket = new Socket();
		try
		{
			socket.connect(new InetSocketAddress(server, 80));
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch(IOException e)
			{
				// do nothing
			}
		}
	}

	public static String readFromStdin(boolean isEcho) throws IOException
	{
		// int len = System.in.read(STDIN_BUFFER);
		// return new String(STDIN_BUFFER, 0, len).trim();

		if(isEcho)
		{
			return console.readLine().trim();
		}
		return new String(console.readPassword()).trim();
	}
}
