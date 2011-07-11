/**
 * This file is part of the hyk-proxy-android project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpsReverseServer.java 
 *
 * @author yinqiwen [ 2011-7-10 | ÏÂÎç06:26:45 ]
 *
 */
package org.hyk.proxy.framework.httpserver.reverse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.hyk.proxy.android.config.Config;
import org.hyk.proxy.framework.common.Misc;
import org.hyk.proxy.framework.util.SimpleSocketAddress;

/**
 *
 */
public class HttpsReverseServer implements Runnable
{
	private SSLServerSocketFactory factory;
	private SSLServerSocket server;
	private boolean running = true;
	private static HttpsReverseServer instance = null;
	
	private HttpsReverseServer(SSLContext ctx) throws IOException
	{
		factory = ctx.getServerSocketFactory();
		server = (SSLServerSocket) factory.createServerSocket();
		server.bind(null);
		Misc.getGlobalThreadPool().submit(this);
	}
	
	public static void initSigletonInstance(SSLContext ctx) throws IOException
	{
		instance = new HttpsReverseServer(ctx);
	}
	
	public static HttpsReverseServer getInstance()
	{
		return instance;
	}
	
	public int getReverseServerPort()
	{
		return server.getLocalPort();
	}
	
	public SocketAddress getReverseServerSocketAddress()
	{
		return server.getLocalSocketAddress();
	}
	
	public void stop()
	{
		running = false;
		InetSocketAddress addr = (InetSocketAddress) getReverseServerSocketAddress();
		try
        {
	        Socket temp = new Socket(addr.getHostName(), getReverseServerPort());
	        temp.close();
        }
        catch (Exception e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}

	@Override
	public void run()
	{
		while (running)
		{
			try
			{
				Socket client = server.accept();
				SimpleSocketAddress addres = Config.getInstance()
				        .getLocalProxyServerAddress();
				Socket localRemote = new Socket(addres.host, addres.port);
				Misc.getGlobalThreadPool().submit(new ForwardTask(client.getInputStream(), localRemote.getOutputStream()));
				Misc.getGlobalThreadPool().submit(new ForwardTask(localRemote.getInputStream(), client.getOutputStream()));
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				running = false;
			}
		}
	}

	public static class ForwardTask implements Runnable
	{
		private InputStream is;
		private OutputStream os;

		public ForwardTask(InputStream is, OutputStream os)
		{
			this.is = is;
			this.os = os;
		}

		@Override
		public void run()
		{
			byte[] buffer = new byte[4096];
			while (true)
			{
				try
				{
					int len = is.read(buffer);
					if (len > 0)
					{
						os.write(buffer, 0, len);
					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					break;
				}

			}
			try
			{
				is.close();
				os.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
