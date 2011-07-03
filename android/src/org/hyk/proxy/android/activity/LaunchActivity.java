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
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 *
 */
public class LaunchActivity extends Activity
{
	private Button triggerbutton;
	private IProxyService proxySrvice;
	private IProxyServiceCallback callback = new IProxyServiceCallback()
	{

		@Override
		public IBinder asBinder()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void statusChanged(String value) throws RemoteException
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void logMessage(String value) throws RemoteException
		{
			StatusHelper.log(value);

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
				if (null != triggerbutton)
				{
					if (proxySrvice.getStatus() == 0)
					{
						triggerbutton.setText("Stop");
						triggerbutton.setCompoundDrawablesWithIntrinsicBounds(
						        R.drawable.player_stop, 0, 0, 0);
					}
				}
				
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
		StatusHelper.log("hyk-proxy-android V0.9.5 launched.", statusView);
		
		
		triggerbutton = (Button) findViewById(R.id.triggerbutton);
		Button cfgbutton = (Button) findViewById(R.id.configbutton);
		Button exitbButton = (Button) findViewById(R.id.exitbutton);
		Button helpButton = (Button) findViewById(R.id.helpbutton);

		Intent intent = new Intent(ProxyService.class.getName());
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		// get
		// triggerbutton.setBackgroundDrawable(Resources.getSystem().getDrawable(R.drawable.player_stop));
		// triggerbutton.setText("Stop");
		// triggerbutton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.player_stop,
		// 0, 0, 0);

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
				System.exit(0);
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
