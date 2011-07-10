/**
 * This file is part of the hyk-proxy-android project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: LaunchService.java 
 *
 * @author yinqiwen [ 2011-6-22 | ÏÂÎç08:54:45 ]
 *
 */
package org.hyk.proxy.android.service;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.Security;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.hyk.proxy.android.config.Config;
import org.hyk.proxy.framework.Framework;
import org.hyk.proxy.framework.httpserver.HttpLocalProxyServer;
import org.hyk.proxy.framework.trace.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.util.ClientUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

/**
 *
 */
public class ProxyService extends Service
{
	protected static Logger logger = LoggerFactory
	        .getLogger(HttpLocalProxyServer.class);
	private IProxyServiceCallback callback = null;
	private Executor frameworkExecutor = Executors.newFixedThreadPool(1);
	private Trace trace = new Trace()
	{
		@Override
		public void notice(String msg)
		{
			try
			{
				if(null != callback)
				callback.logMessage(msg);
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void info(String msg)
		{
			try
			{
				if(null != callback)
				callback.logMessage(msg);
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void error(String msg)
		{
			try
			{
				if(null != callback)
				callback.logMessage(msg);
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	private Framework fr = null;

	private IBinder binder = new IProxyService.Stub()
	{

		@Override
		public void registerCallback(IProxyServiceCallback cb)
		        throws RemoteException
		{
			callback = cb;
		}

		@Override
		public int getStatus() throws RemoteException
		{
			if (fr.isStarted())
			{
				return 1;
			}
			else if (fr.isStarting())
			{
				return 2;
			}
			return 0;
		}

		@Override
		public void start() throws RemoteException
		{
			Config.initSingletonInstance(ProxyService.this);
			System.setProperty("java.net.preferIPv4Stack", "true");
			System.setProperty("java.net.preferIPv6Addresses", "false");
//			
			
			final IProxyServiceCallback tempcb = callback;
			frameworkExecutor.execute(new Runnable()
			{
				@Override
				public void run()
				{
					fr.start();
					if (null != callback)
					{
						try
                        {
	                        tempcb.statusChanged(getStatus());
                        }
                        catch (RemoteException e)
                        {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
                        }
					}
				}
			});
			
		}

		@Override
		public void stop() throws RemoteException
		{
			final IProxyServiceCallback tempcb = callback;
			frameworkExecutor.execute(new Runnable()
			{
				@Override
				public void run()
				{
					fr.stop();
					if (null != callback)
					{
						try
                        {
	                        callback.statusChanged(getStatus());
                        }
                        catch (RemoteException e)
                        {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
                        }
					}
				}
			});
		
		}

		@Override
		public void unregisterCallback(IProxyServiceCallback cb)
		        throws RemoteException
		{
			callback = null;
		}
	};

	@Override
	public void onCreate()
	{
		super.onCreate();
		ClientUtils.assetManager = getAssets();
		Config.initSingletonInstance(this);
		fr = Framework.getInstance(trace);
	}

	@Override
	public IBinder onBind(Intent arg)
	{
		return binder;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		fr.stop();
	}

}
