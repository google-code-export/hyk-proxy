/**
 * 
 */
package com.hyk.proxy.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 * 
 */
public class GoogleAvailableService
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private List<String> defaultHttpsServiceAddress = Arrays.asList(
	        "www.google.com.hk", "mail.google.com", "code.google.com",
	        "www.google.com.tw", "www.google.co.jp", "docs.google.com",
	        "translate.google.com", "picasa.google.com");

	private List<String> defaultHttpServiceAddress = Arrays.asList(
	        "www.google.cn", "translate.google.cn", "code.google.com",
	        "www.google.com.tw", "www.google.co.jp", "docs.google.com",
	        "translate.google.com", "picasa.google.com", "mail.google.com");

	private Properties hostsProps = new Properties();
	private static GoogleAvailableService instance = new GoogleAvailableService();

	public static GoogleAvailableService getInstance()
	{
		return instance;
	}

	private GoogleAvailableService()
	{
		try
		{
			InputStream resource = GoogleAvailableService.class
			        .getResourceAsStream("/GoogleMappingHosts.txt");
			if (null != resource)
			{
				try
				{
					hostsProps.load(resource);
					hostsProps.list(System.out);
				}
				catch (IOException e)
				{

				}
			}

			InputStream http_resource = GoogleAvailableService.class
			        .getResourceAsStream("/GoogleHttpHosts.txt");
			if (null != http_resource)
			{
				try
				{
					BufferedReader r = new BufferedReader(
					        new InputStreamReader(http_resource));
					String line = null;
					List<String> new_list = new ArrayList<String>();
					while ((line = r.readLine()) != null)
					{
						line = line.trim();
						if (line.startsWith("#"))
						{
							continue;
						}
						new_list.add(line);
					}
					new_list.addAll(defaultHttpServiceAddress);
					defaultHttpServiceAddress = new_list;
				}
				catch (IOException e)
				{

				}
			}
			InputStream https_resource = GoogleAvailableService.class
			        .getResourceAsStream("/GoogleHttpsHosts.txt");
			if (null != https_resource)
			{
				try
				{
					BufferedReader r = new BufferedReader(
					        new InputStreamReader(https_resource));
					String line = null;
					List<String> new_list = new ArrayList<String>();
					while ((line = r.readLine()) != null)
					{
						line = line.trim();
						if (line.startsWith("#"))
						{
							continue;
						}
						new_list.add(line);

					}
					new_list.addAll(defaultHttpsServiceAddress);
					defaultHttpsServiceAddress = new_list;
				}
				catch (IOException e)
				{

				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public String getMappingHost(String host)
	{
		if (hostsProps.containsKey(host))
		{
			return hostsProps.getProperty(host);
		}
		return host;
	}

	public String getAvailableHttpsService()
	{
		for (String service : defaultHttpsServiceAddress)
		{
			try
			{
				Socket s = new Socket(service, 443);
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
