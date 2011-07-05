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

import org.hyk.proxy.android.config.Config;
import org.hyk.proxy.framework.Framework;
import org.hyk.proxy.framework.httpserver.HttpLocalProxyServer;
import org.hyk.proxy.framework.trace.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	protected static Logger	logger	= LoggerFactory.getLogger(HttpLocalProxyServer.class);
	private IProxyServiceCallback callback = null;

	private Trace trace = new Trace()
	{
		
		@Override
		public void notice(String msg)
		{
			try
            {
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
			// TODO Auto-generated method stub
			return fr.isStarted()?1:0;
		}


		@Override
        public void start() throws RemoteException
        {
			Config.initSingletonInstance(ProxyService.this);
			String x = Config.getInstance().getLocalProxyServerAddress().host;
			logger.info("########" +x);
			//fr.start();
        }

		@Override
        public void stop() throws RemoteException
        {
			fr.stop();
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
		Config.initSingletonInstance(this);
		fr = new Framework(trace);
		
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
	}

}
