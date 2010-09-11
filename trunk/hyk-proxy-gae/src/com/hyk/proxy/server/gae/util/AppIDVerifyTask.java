/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppIDVerifyTask.java 
 *
 * @author yinqiwen [ 2010-8-30 | ÏÂÎç10:22:04 ]
 *
 */
package com.hyk.proxy.server.gae.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.hyk.proxy.server.gae.appid.AppIdShareItem;
import com.hyk.proxy.server.gae.appid.AppIdTaskStatus;

/**
 *
 */
public class AppIDVerifyTask
{
	protected static Logger				logger		= LoggerFactory.getLogger(AppIDVerifyTask.class);
	
	private static void removeShareItem(AppIdShareItem item)
	{
		logger.error("Try to remove share item with appid:" + item.getAppid());
		//ServerUtils.ofy.delete(AppIdShareItem.class, item.getAppid());
    	if(null != item.getGmail())
    	{
    		String address = item.getAppid() + ".appspot.com";
    		StringBuffer buffer = new StringBuffer();
    		buffer.append("We noticed that ").append(address).append(" is not a valid hyk-proxy-gae server address.").append("\n");
    		buffer.append("So we removed it from the main database. ");
    		EMailUtil.sendMail(item.getGmail(), buffer.toString());
    	}
	}
	
	public static void verifyAppIDs()
	{
		URLFetchService	urlFetchService = URLFetchServiceFactory.getURLFetchService();
		QueryResultIterable<AppIdShareItem> results = ServerUtils.ofy.query(AppIdShareItem.class).fetch();
		Map<String, Future<HTTPResponse>> futureResults = new HashMap<String, Future<HTTPResponse>>();
		//ArrayList<Future<HTTPResponse>> futureResults = new ArrayList<Future<HTTPResponse>>();
		int totalCount = ServerUtils.ofy.query(AppIdShareItem.class).countAll();
		int maxVerifyNum = 50;
		int cursor = 0;
		for(AppIdShareItem item : results)
		{
			AppIdTaskStatus status = ServerUtils.ofy.find(AppIdTaskStatus.class, item.getAppid());
			if(null == status || !status.isFinished())
			{
				try
	            {
					String urlstr = "http://" + item.getAppid() + ".appspot.com";
					futureResults.put(item.getAppid(), urlFetchService.fetchAsync(new URL(urlstr)));
	            }
	            catch (Exception e)
	            {
		            logger.error("Failed to fetch.", e);
	            }
			}
		}
		
		for(String appid:futureResults.keySet())
		{
			if(cursor >= maxVerifyNum && cursor < totalCount)
			{
				Queue queue = QueueFactory.getDefaultQueue();
				TaskOptions task = TaskOptions.Builder.url("/clear-stat-records").method(Method.GET);
				queue.add(task);
				return;
			}
			AppIdShareItem item = ServerUtils.ofy.find(AppIdShareItem.class, appid);
			try
            {
	            HTTPResponse res = futureResults.get(appid).get();
	            String str = new String(res.getContent());
	           
	            if(!str.contains("hyk-proxy"))
	            {
	            	removeShareItem(item);
	            }
            }
            catch (InterruptedException e)
            {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
            catch (Exception e)
            {
            	removeShareItem(item);
            }
            finally
            {
            	AppIdTaskStatus status = new AppIdTaskStatus();
            	status.setAppid(appid);
            	status.setFinished(true);
            	ServerUtils.ofy.put(status);
            }
		}
		ServerUtils.deleteType(AppIdTaskStatus.class);
	}
}
