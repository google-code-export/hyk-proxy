/**
 * This file is part of the hyk-proxy-android project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: LaunchService.java 
 *
 * @author yinqiwen [ 2011-6-22 | обнГ08:54:45 ]
 *
 */
package org.hyk.proxy.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 *
 */
public class LaunchService extends Service
{

	@Override
	public void onCreate()
	{
	    super.onCreate();
	    
	}
	
	@Override
    public IBinder onBind(Intent arg)
    {
	    // TODO Auto-generated method stub
	    return null;
    }

}
