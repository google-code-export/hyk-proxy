/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: MasterNodeService.java 
 *
 * @author yinqiwen [ 2011-12-7 | ÏÂÎç10:24:13 ]
 *
 */
package org.hyk.proxy.gae.server.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.Id;

import org.arch.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

/**
 *
 */
public class MasterNodeService
{
	protected static Logger logger = LoggerFactory
	        .getLogger(MasterNodeService.class);

	private static CapabilitiesService capabilities = CapabilitiesServiceFactory
	        .getCapabilitiesService();
	private static DatastoreService datastore = DatastoreServiceFactory
	        .getDatastoreService();
	private static AsyncDatastoreService asyncdatastore = DatastoreServiceFactory
	        .getAsyncDatastoreService();
	protected static AsyncMemcacheService asyncCache = MemcacheServiceFactory
	        .getAsyncMemcacheService();
	protected static MemcacheService cache = MemcacheServiceFactory
	        .getMemcacheService();
	protected static Map<String, Object> localMemCache = new ConcurrentHashMap<String, Object>();

	public static class AppIdShareItem
	{
		@Id
		private String appid;

		public String getAppid()
		{
			return appid;
		}

		public void setAppid(String appid)
		{
			this.appid = appid;
		}

		public String getGmail()
		{
			return gmail;
		}

		public void setGmail(String gmail)
		{
			this.gmail = gmail;
		}

		private String gmail;
	}

	public static List<String> randomRetrieveAppIds()
	{

		// List<AppIdShareItem> ret = new ArrayList<AppIdShareItem>();
		// for (AppIdShareItem ro : results)
		// {
		// ret.add(ro);
		// }
		// Collections.shuffle(ret);
		// int maxRet = 2;
		// List<String> appids = new ArrayList<String>();
		// for (int i = 0; i < ret.size() && i < maxRet; i++)
		// {
		// appids.add(ret.get(i).getAppid());
		// }
		// return appids;
		return null;
	}

	private static boolean verifyAppId(String appid)
	{
		try
		{
			String urlstr = "http://" + appid + ".appspot.com";
			URL url = new URL(urlstr);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
			        url.openStream()));
			StringBuffer buf = new StringBuffer();
			String line;

			while ((line = reader.readLine()) != null)
			{
				buf.append(line);
			}
			reader.close();
			return buf.toString().contains("hyk-proxy");

		}
		catch (Exception e)
		{
			// ...
		}
		return false;
	}

	public static String shareMyAppId(String appid, String gmail)
	{
		AppIdShareItem share = getSharedItem(appid);
		if (null != share)
		{
			return "This AppId is already shared!";
		}
		if (!verifyAppId(appid))
		{
			return "Invalid AppId or Invalid hyk-proxy-server for this AppId!";
		}
		// currently, no check for gmail
		share = new AppIdShareItem();
		share.setAppid(appid);
		share.setGmail(gmail);
		saveSharedItem(share);
		if (null != gmail)
		{
			sendMail(gmail, "Thanks for sharing AppID:" + appid + "!",
			        "Thank you for sharing your appid!");
		}
		return "Share AppId Success!";
	}

	public static String unshareMyAppid(String appid, String gmail)
	{
		AppIdShareItem share = getSharedItem(appid);
		if (null == share)
		{
			return "This appid is not shared before!";
		}
		if (null != share.getGmail())
		{
			if (!share.getGmail().equals(gmail))
			{
				return "The input email address is not equal the share email address.";
			}
		}
		saveSharedItem(share);
		return "Unshare AppId Success!";
	}

	private static AppIdShareItem getSharedItem(String appid)
	{
		return null;
	}

	private static void saveSharedItem(AppIdShareItem item)
	{

	}

	private static void sendMail(String toAddress, String subject,
	        String content)
	{
		try
		{
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			Message msg = new MimeMessage(session);
			StringBuffer buffer = new StringBuffer();
			buffer.append("Hi, ").append("\r\n\r\n");
			buffer.append(content);

			buffer.append("Thanks again. admin@"
			        + SystemProperty.applicationId.get() + ".appspot.com");
			String msgBody = buffer.toString();
			msg.setSubject(subject);
			msg.setFrom(new InternetAddress("admin@"
			        + SystemProperty.applicationId.get() + ".appspotmail.com"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
			        toAddress, "Mr/Ms. User"));
			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(
			        "yinqiwen@gmail.com", "Mr. Admin"));
			// msg.set
			msg.setText(msgBody);
			Transport.send(msg);
		}
		catch (Exception e)
		{
			logger.error("Failed to send mail to user:" + toAddress, e);
		}
	}

}
