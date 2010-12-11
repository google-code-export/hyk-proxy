/**
 * 
 */
package com.hyk.proxy.client.util;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 * 
 */
public class GoogleAvailableService
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private String[] defaultHttpsServiceAddress = { "www.google.com.hk",
	        "mail.google.com", "code.google.com", "www.google.com.tw",
	        "www.google.co.jp", "docs.google.com", "translate.google.com",
	        "picasa.google.com" };

	private String[] defaultHttpServiceAddress = { "www.google.cn",
	        "translate.google.cn", "code.google.com", "www.google.com.tw",
	        "www.google.co.jp", "docs.google.com", "translate.google.com",
	        "picasa.google.com", "mail.google.com" };

	private static GoogleAvailableService instance = new GoogleAvailableService();

	public static GoogleAvailableService getInstance()
	{
		return instance;
	}

	private GoogleAvailableService()
	{
	}

	public String getAvailableHttpsService()
	{
		for (String service : defaultHttpsServiceAddress)
		{
			try
			{
				Socket s = new Socket(service, 80);
				s.close();
				return service;
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
		return null;
	}

	public String getAvailableHttpService()
	{
		for (String service : defaultHttpServiceAddress)
		{
			try
			{
				Socket s = new Socket(service, 80);
				s.close();
				return service;
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
		return null;
	}
}
