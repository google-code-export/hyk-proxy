/**
 * This file is part of the hyk-proxy-android project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ConfigurationActivity.java 
 *
 * @author yinqiwen [ 2011-6-25 | ÏÂÎç09:32:17 ]
 *
 */
package org.hyk.proxy.android.activity;

import org.hyk.proxy.android.R;

import android.app.TabActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TabHost;

/**
 *
 */
public class ConfigurationActivity extends PreferenceActivity implements
        OnPreferenceClickListener
{
	// private TabHost tabhost;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences prefs = PreferenceManager
		        .getDefaultSharedPreferences(this);
	}

	@Override
	public boolean onPreferenceClick(Preference arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
