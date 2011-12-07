/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Admin.java 
 *
 * @author yinqiwen [ 2010-4-9 | 06:59:44 PM]
 *
 */
package org.hyk.proxy.gae.client.admin;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.hyk.proxy.gae.client.admin.handler.AddGroup;
import org.hyk.proxy.gae.client.admin.handler.AddUser;
import org.hyk.proxy.gae.client.admin.handler.Blacklist;
import org.hyk.proxy.gae.client.admin.handler.ChangePasswd;
import org.hyk.proxy.gae.client.admin.handler.ClearScreen;
import org.hyk.proxy.gae.client.admin.handler.CommandHandler;
import org.hyk.proxy.gae.client.admin.handler.ConfigServer;
import org.hyk.proxy.gae.client.admin.handler.DeleteGroup;
import org.hyk.proxy.gae.client.admin.handler.DeleteUser;
import org.hyk.proxy.gae.client.admin.handler.Exit;
import org.hyk.proxy.gae.client.admin.handler.Help;
import org.hyk.proxy.gae.client.admin.handler.ListGroups;
import org.hyk.proxy.gae.client.admin.handler.ListUsers;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.GAEServerAuth;
import org.hyk.proxy.gae.client.connection.ProxyConnection;
import org.hyk.proxy.gae.client.connection.ProxyConnectionManager;
import org.hyk.proxy.gae.client.plugin.GAE;
import org.hyk.proxy.gae.common.CompressorType;
import org.hyk.proxy.gae.common.EncryptType;
import org.hyk.proxy.gae.common.GAEPluginVersion;
import org.hyk.proxy.gae.common.auth.User;
import org.hyk.proxy.gae.common.event.GAEEvents;

/**
 *
 */
public class GAEAdmin implements Runnable
{
	private static final String PROMOTE = "$ ";

	private User userInfo;

	private Map<String, CommandHandler> handlers = new HashMap<String, CommandHandler>();
	
	private ProxyConnection connection;

	public static void err(String msg)
	{
		System.err.print(msg);
	}

	public static void errln(String msg)
	{
		System.err.println(msg);
	}

	public static void output(String msg)
	{
		if (null != msg)
		{
			System.out.print(msg);
		}
	}

	public static void outputln(String msg)
	{
		if (null != msg)
		{
			System.out.println(msg);
		}
	}

	public static void exit(String msg)
	{
		errln(msg);
		System.console().printf("Press <Enter> to exit...");
		System.console().readLine();
		System.exit(1);
	}

	protected void registerCommandHandler(String user)
	{
		handlers.put(Exit.COMMAND, new Exit());
		handlers.put(ClearScreen.COMMAND, new ClearScreen());
		handlers.put(ChangePasswd.COMMAND, new ChangePasswd(connection,user));
		handlers.put(AddUser.COMMAND, new AddUser(connection));
		handlers.put(AddGroup.COMMAND, new AddGroup(connection));
		handlers.put(ListUsers.COMMAND, new ListUsers(connection));
		handlers.put(ListGroups.COMMAND, new ListGroups(connection));
		handlers.put(DeleteUser.COMMAND, new DeleteUser(connection));
		handlers.put(DeleteGroup.COMMAND, new DeleteGroup(connection));
		handlers.put(Blacklist.COMMAND, new Blacklist(connection));
		handlers.put(ConfigServer.COMMAND, new ConfigServer(connection));
//		handlers.put(Traffic.COMMAND, new Traffic());
//		handlers.put(Stat.COMMAND, new Stat(userInfo));
		handlers.put(Help.COMMAND, new Help());
	}

	public void run()
	{
		try
		{
			output("appid:");
			String appid = System.console().readLine();
			output("login:");
			String user = System.console().readLine();
			output("password:");
			String passwd = new String(System.console().readPassword());
			GAEEvents.init(null, false);
			GAEServerAuth auth = new GAEServerAuth();
			auth.appid = appid;
			auth.user = user.trim();
			auth.passwd = passwd.trim();
			GAEClientConfiguration.getInstance().setGAEServerAuths(Arrays.asList(auth));
			GAEClientConfiguration.getInstance().setCompressorType(CompressorType.NONE);
			GAEClientConfiguration.getInstance().setEncrypterType(EncryptType.NONE);
			if(!GAE.initProxyConnections(Arrays.asList(auth)))
			{
				GAEAdmin.exit("");
			}

			connection = ProxyConnectionManager.getInstance().getClientConnection(null);
			this.userInfo = new User();
			userInfo.setEmail(user);
			userInfo.setPasswd(passwd);

			// BandwidthStatisticsService bandwidthStatisticsService = null;

			InputStream is = getClass().getResourceAsStream("welcome.txt");
			byte[] buffer = new byte[4096];
			int len = is.read(buffer);
			String format = new String(buffer, 0, len);
			outputln(String.format(format, GAEPluginVersion.value, Help.USAGE,
			        GAEPluginVersion.value, GAEPluginVersion.value));

			registerCommandHandler(user);

			while (true)
			{
				outputln(user + "@" + appid + " ~");
				output(PROMOTE);
				String[] commands = System.console().readLine().split(
				        "\\s+");
				if (null == commands || commands.length == 0
				        || commands[0].trim().equals(""))
				{
					continue;
				}
				String[] args = new String[commands.length - 1];
				System.arraycopy(commands, 1, args, 0, args.length);
				CommandHandler handler = handlers.get(commands[0].trim());
				if (null == handler)
				{
					outputln("-admin: " + commands[0] + ": command not found");
					continue;
				}
				handler.execute(args);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			GAEAdmin.exit(e.getMessage());
		}
	}
}
