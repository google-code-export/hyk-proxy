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

	private String[] targetServiceAddress = { "mail.google.com",
	        "code.google.com", "www.google.com.hk", "www.google.cn",
	        "www.google.com.tw", "www.google.co.jp", "docs.google.com",
	        "translate.google.com", "picasa.google.com" };

	private String fastestService = null;

	private static GoogleAvailableService instance = new GoogleAvailableService();

	public static GoogleAvailableService getInstance()
	{
		return instance;
	}

	private GoogleAvailableService()
	{
		new Thread(new AvailableServiceChecker()).start();
	}

	public String getFastestAvailableService()
	{
		return fastestService;
	}

	class AvailableServiceChecker implements Runnable
	{
		@Override
		public void run()
		{
			while (true)
			{
				long fastest = 0;
				for (String addr : targetServiceAddress)
				{
					long start = System.currentTimeMillis();
					try
					{
						Socket s = new Socket(addr, 80);
						s.close();
					}
					catch (Throwable e)
					{
						continue;
					}
					long end = System.currentTimeMillis();
					if (logger.isDebugEnabled())
					{
						logger.debug("Service:" + addr + " has connect time:"
						        + (end - start) + "ms.");
					}
					if (fastest == 0)
					{
						fastest = end - start;
					}
					else
					{
						if ((end - start) < fastest)
						{
							fastest = end - start;
							fastestService = addr;
						}
					}
				}
				try
				{
					Thread.sleep(3000000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
