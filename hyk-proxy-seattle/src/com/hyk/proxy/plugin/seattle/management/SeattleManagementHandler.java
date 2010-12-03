/**
 * 
 */
package com.hyk.proxy.plugin.seattle.management;

import java.io.Console;
import java.net.Socket;
import java.util.List;

import com.hyk.proxy.framework.common.Version;
import com.hyk.proxy.framework.management.ManageResource;
import com.hyk.proxy.framework.plugin.PluginAdmin;
import com.hyk.proxy.framework.util.SimpleSocketAddress;
import com.hyk.proxy.plugin.seattle.config.SeattleApplicationConfig;
import com.hyk.proxy.plugin.seattle.event.SeattleProxyEventServiceFactory;

/**
 * @author qiyingwang
 * 
 */
public class SeattleManagementHandler implements ManageResource, PluginAdmin
{

	public static void output(String msg)
	{
		if (null != msg)
		{
			System.out.print(msg);
		}
	}

	@Override
	public String handleManagementCommand(String cmd)
	{
		return null;
	}

	@Override
	public String getName()
	{
		return SeattleProxyEventServiceFactory.NAME;
	}
	
	private void showServerStatus()
	{
		List<SimpleSocketAddress> addrs = SeattleApplicationConfig.getSeattleServerAddress();
		for(SimpleSocketAddress addr:addrs)
		{
			output(addr.toString());
			output("           ");
			try
			{
				long start = System.currentTimeMillis();
				Socket s = new Socket(addr.host, addr.port);
				s.close();
				long end = System.currentTimeMillis();
				output((end-start) + "ms");
			}
			catch (Exception e)
			{
				output("Not available");
			}
			System.out.println();
		}
	}

	@Override
	public void start()
	{
		Console console = System.console();
		while (true)
		{
			System.out.println("==============SeattleGENI Admin=============");
			System.out.println("[1] List Remote Servers");
			System.out.println("[0] Return");
			System.out.print("Please enter 0-1:");
			String s = console.readLine();
			try
			{
				int choice = Integer.parseInt(s);
				if (0 == choice)
				{
					break;
				}
				else if (1 == choice)
				{
					showServerStatus();
					continue;
				}
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
			System.err.println("Wrong input:" + s);
		}
	}

}
