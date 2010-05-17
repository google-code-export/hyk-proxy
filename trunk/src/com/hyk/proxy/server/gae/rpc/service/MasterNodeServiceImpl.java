/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppIdShareServiceImpl.java 
 *
 * @author qiying.wang [ May 17, 2010 | 5:23:19 PM ]
 *
 */
package com.hyk.proxy.server.gae.rpc.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.hyk.proxy.common.Version;
import com.hyk.proxy.common.rpc.service.MasterNodeService;
import com.hyk.proxy.server.gae.appid.AppIdShareItem;
import com.hyk.proxy.server.gae.util.ServerUtils;

/**
 *
 */
public class MasterNodeServiceImpl implements MasterNodeService, Serializable
{

	@Override
	public List<String> randomRetrieveAppIds()
	{
		QueryResultIterable<AppIdShareItem> results = ServerUtils.ofy.query(AppIdShareItem.class).fetch();
		List<AppIdShareItem> ret = new ArrayList<AppIdShareItem>();
		for(AppIdShareItem ro : results)
		{
			ret.add(ro);
		}
		Collections.shuffle(ret);
		int maxRet = 5;
		List<String> appids = new ArrayList<String>();
		for(int i = 0; i < ret.size() && i < maxRet ; i++)
		{
			appids.add(ret.get(i).getAppid());
		}
		return appids;
	}

	private boolean verifyAppId(String appid)
	{
		try
		{
			String urlstr = "http://" + appid + ".appspot.com";
			URL url = new URL(urlstr);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buf = new StringBuffer();
			String line;

			while((line = reader.readLine()) != null)
			{
				buf.append(line);
			}
			reader.close();
			return buf.toString().contains("hyk-proxy");

		}
		catch(Exception e)
		{
			// ...
		}
		return false;
	}

	@Override
	public String shareMyAppId(String appid, String gmail)
	{
		AppIdShareItem share = ServerUtils.ofy.find(AppIdShareItem.class, appid);
		if(null != share)
		{
			return "This AppId is already shared!";
		}
		if(!verifyAppId(appid))
		{
			return "Invalid AppId or Invalid hyk-proxy-server for this AppId!";
		}
		// currently, no check for gmail
		share = new AppIdShareItem();
		share.setAppid(appid);
		share.setGmail(gmail);
		ServerUtils.storeObject(share);
		return "Share AppId Success!";
	}

	@Override
	public String unshareMyAppid(String appid, String gmail)
	{
		AppIdShareItem share = ServerUtils.ofy.find(AppIdShareItem.class, appid);
		// currently, no check for gmail
		if(null == share)
		{
			return "This appid is not shared before!";
		}
		ServerUtils.deleteObject(share);
		return "Unshare AppId Success!";
	}

	@Override
	public String getVersion()
	{
		return Version.value;
	}

}
