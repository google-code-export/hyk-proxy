/**
 * This file is part of the hyk-proxy-android project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: LaunchActivity.java 
 *
 * @author yinqiwen [ 2011-6-22 | ÏÂÎç09:01:18 ]
 *
 */
package org.hyk.proxy.activity;

import org.hyk.proxy.framework.R;

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
import android.widget.ImageView;
import android.widget.TextView;

/**
 *
 */
public class LaunchActivity extends Activity implements OnLongClickListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
	    // TODO Auto-generated method stub
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.launch);
	}

	@Override
    public boolean onLongClick(View arg0)
    {
	    // TODO Auto-generated method stub
	    return false;
    }
}
