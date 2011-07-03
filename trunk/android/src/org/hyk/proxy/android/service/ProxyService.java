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

import org.hyk.proxy.framework.Framework;
import org.hyk.proxy.framework.trace.Trace;

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
	
	private Framework fr = new Framework(trace);
	
	private IBinder binder = new IProxyService.Stub()
	{

		@Override
		public void registerCallback(IProxyServiceCallback cb)
		        throws RemoteException
		{
			callback = cb;
			callback.logMessage("@@@@@@@@@@@@@@@");
		}

		@Override
		public int getStatus() throws RemoteException
		{
			// TODO Auto-generated method stub
			return 0;
		}


		@Override
        public void start() throws RemoteException
        {
			fr.start();
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
	        // TODO Auto-generated method stub
	        
        }
	};

	@Override
	public void onCreate()
	{
		super.onCreate();
        
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
