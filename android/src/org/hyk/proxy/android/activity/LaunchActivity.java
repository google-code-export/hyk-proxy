/**
 * This file is part of the hyk-proxy-android project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: LaunchActivity.java 
 *
 * @author yinqiwen [ 2011-6-22 | ÏÂÎç09:01:18 ]
 *
 */
package org.hyk.proxy.android.activity;

import org.hyk.proxy.android.R;
import org.hyk.proxy.android.helper.StatusHelper;
import org.hyk.proxy.android.service.IProxyService;
import org.hyk.proxy.android.service.IProxyServiceCallback;
import org.hyk.proxy.android.service.ProxyService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 *
 */
public class LaunchActivity extends Activity
{
	private Button triggerbutton;
	private IProxyService proxySrvice;
	private StatusHelper statusHelper;
	private Handler handler = new Handler();
	private void statusChanged(final int value)
	{
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (value == 0)
				{
					triggerbutton.setEnabled(true);
					triggerbutton.setText("Start");
					triggerbutton.setCompoundDrawablesWithIntrinsicBounds(
					        R.drawable.player_play, 0, 0, 0);
				}
				else
				{
					triggerbutton.setEnabled(true);
					triggerbutton.setText("Stop");
					triggerbutton.setCompoundDrawablesWithIntrinsicBounds(
					        R.drawable.player_stop, 0, 0, 0);
				}	
				if (value == 1)
				{
					statusHelper.log("Local HTTP(s) proxy server is started.");
				}
				else if (value == 0)
				{
					statusHelper.log("Local HTTP(s) proxy server is stoped.");
				}
				else
				{
					statusHelper.log("Local HTTP(s) proxy server is starting.");
				}
			}
		});
		
	}

	private IProxyServiceCallback callback = new IProxyServiceCallback()
	{

		@Override
		public IBinder asBinder()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void statusChanged(int value) throws RemoteException
		{
			LaunchActivity.this.statusChanged(value);
		}

		@Override
		public void logMessage(String value) throws RemoteException
		{
			statusHelper.log(value);
		}
	};

	private ServiceConnection serviceConnection = new ServiceConnection()
	{

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			proxySrvice = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			proxySrvice = IProxyService.Stub.asInterface(service);
			try
			{
				proxySrvice.registerCallback(callback);
				int status = proxySrvice.getStatus();
				statusChanged(status);
			}
			catch (RemoteException e)
			{
				Log.e("", "", e);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_main);

		final TextView statusView = (TextView) findViewById(R.id.statusview);
		statusHelper = new StatusHelper(statusView, handler);
		statusHelper.log("hyk-proxy-android V0.9.4 launched.");

		triggerbutton = (Button) findViewById(R.id.triggerbutton);
		Button cfgbutton = (Button) findViewById(R.id.configbutton);
		Button exitbButton = (Button) findViewById(R.id.exitbutton);
		Button helpButton = (Button) findViewById(R.id.helpbutton);

		Intent intent = new Intent(ProxyService.class.getName());
		startService(intent);
		
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		triggerbutton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (null != proxySrvice)
				{
					try
					{
						// changeTriggerButtonView();
						int status = proxySrvice.getStatus();
						if (status == 0)
						{
							triggerbutton.setEnabled(false);
							//triggerbutton.setText("Starting");
							proxySrvice.start();
						}
						else if (status == 1)
						{
							proxySrvice.stop();
							unbindService(serviceConnection);
							stopService(new Intent(ProxyService.class.getName()));
						}
						else if (status == 2)
						{
							return;
						}
					}
					catch (Exception e)
					{
						// TODO: handle exception
					}

				}

			}
		});

		cfgbutton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivityForResult(new Intent(LaunchActivity.this,
				        ConfigurationActivity.class), 1);
			}
		});

		helpButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Uri uri = Uri.parse("http://code.google.com/p/hyk-proxy/");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});

		exitbButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				LaunchActivity.this.finish();
			}
		});
	}

	@Override
	protected void onDestroy()
	{
		if (proxySrvice != null)
		{
			try
			{
				proxySrvice.unregisterCallback(callback);
			}
			catch (RemoteException e)
			{
			}
		}
		unbindService(serviceConnection);
		super.onDestroy();
	}

}
