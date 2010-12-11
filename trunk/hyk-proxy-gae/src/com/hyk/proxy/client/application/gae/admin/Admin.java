/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Admin.java 
 *
 * @author yinqiwen [ 2010-4-9 | 06:59:44 PM]
 *
 */
package com.hyk.proxy.client.application.gae.admin;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.hyk.proxy.client.application.gae.admin.handler.AddGroup;
import com.hyk.proxy.client.application.gae.admin.handler.AddUser;
import com.hyk.proxy.client.application.gae.admin.handler.Blacklist;
import com.hyk.proxy.client.application.gae.admin.handler.ChangePasswd;
import com.hyk.proxy.client.application.gae.admin.handler.ClearScreen;
import com.hyk.proxy.client.application.gae.admin.handler.CommandHandler;
import com.hyk.proxy.client.application.gae.admin.handler.DeleteGroup;
import com.hyk.proxy.client.application.gae.admin.handler.DeleteUser;
import com.hyk.proxy.client.application.gae.admin.handler.Exit;
import com.hyk.proxy.client.application.gae.admin.handler.Help;
import com.hyk.proxy.client.application.gae.admin.handler.ListGroups;
import com.hyk.proxy.client.application.gae.admin.handler.ListUsers;
import com.hyk.proxy.client.application.gae.admin.handler.Stat;
import com.hyk.proxy.client.application.gae.admin.handler.Traffic;
import com.hyk.proxy.client.application.gae.config.Config;
import com.hyk.proxy.client.application.gae.config.Config.ConnectionMode;
import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.ExtensionsLauncher;
import com.hyk.proxy.common.Version;
import com.hyk.proxy.common.gae.auth.User;
import com.hyk.proxy.common.http.message.HttpServerAddress;
import com.hyk.proxy.common.rpc.service.AccountService;
import com.hyk.proxy.common.rpc.service.RemoteServiceManager;
import com.hyk.proxy.common.xmpp.XmppAddress;
import com.hyk.proxy.framework.plugin.PluginAdmin;
import com.hyk.rpc.core.RPC;

/**
 *
 */
public class Admin implements PluginAdmin
{
	private static final String PROMOTE = "$ ";

	private User userInfo;
	private RemoteServiceManager remoteServiceManager = null;

	private Map<String, CommandHandler> handlers = new HashMap<String, CommandHandler>();

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
		AccountService accountService = null;
		try
		{
			accountService = remoteServiceManager.getAccountService(userInfo);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			exit(e.getMessage());
		}

		handlers.put(Exit.COMMAND, new Exit());
		handlers.put(ClearScreen.COMMAND, new ClearScreen());
		handlers.put(ChangePasswd.COMMAND, new ChangePasswd(user,
		        accountService));
		handlers.put(AddUser.COMMAND, new AddUser(accountService));
		handlers.put(AddGroup.COMMAND, new AddGroup(accountService));
		handlers.put(ListUsers.COMMAND, new ListUsers(accountService));
		handlers.put(ListGroups.COMMAND, new ListGroups(accountService));
		handlers.put(DeleteUser.COMMAND, new DeleteUser(accountService));
		handlers.put(DeleteGroup.COMMAND, new DeleteGroup(accountService));
		handlers.put(Blacklist.COMMAND, new Blacklist(accountService));
		handlers.put(Traffic.COMMAND, new Traffic(accountService));
		handlers.put(Stat.COMMAND, new Stat(remoteServiceManager, userInfo));
		handlers.put(Help.COMMAND, new Help());
	}

	public void start()
	{
		try
		{
			ExtensionsLauncher.init();
			Config config = Config.getInstance();
			ClientUtils.selectDefaultGoogleProxy();
			output("appid:");
			String appid = ClientUtils.readFromStdin(true);
			output("login:");
			String user = ClientUtils.readFromStdin(true);
			output("password:");
			String passwd = ClientUtils.readFromStdin(false);

			Executor executor = Executors.newCachedThreadPool();

			if (config.getClient2ServerConnectionMode().equals(
			        ConnectionMode.HTTP2GAE)
			        || config.getClient2ServerConnectionMode().equals(
			                ConnectionMode.HTTPS2GAE))
			{
				RPC rpc = ClientUtils.createHttpRPC(executor);
				remoteServiceManager = rpc.getRemoteService(
				        RemoteServiceManager.class, RemoteServiceManager.NAME,
				        ClientUtils.createHttpServerAddress(appid));
				// remoteServiceManager =
				// rpc.getRemoteService(RemoteServiceManager.class,
				// RemoteServiceManager.NAME, new HttpServerAddress("localhost",
				// 8888, "/fetchproxy"));
			}
			else
			{
				RPC rpc = ClientUtils.createXmppRPC(config.getXmppAccounts()
				        .get(0), executor);
				remoteServiceManager = rpc.getRemoteService(
				        RemoteServiceManager.class, RemoteServiceManager.NAME,
				        new XmppAddress(appid + "@appspot.com"));
			}

			this.userInfo = new User();
			userInfo.setEmail(user);
			userInfo.setPasswd(passwd);

			// BandwidthStatisticsService bandwidthStatisticsService = null;

			InputStream is = getClass().getResourceAsStream("welcome.txt");
			byte[] buffer = new byte[4096];
			int len = is.read(buffer);
			String format = new String(buffer, 0, len);
			outputln(String.format(format, Version.value, Help.USAGE,
			        Version.value, Version.value));

			registerCommandHandler(user);

			while (true)
			{
				outputln(user + "@" + appid + " ~");
				output(PROMOTE);
				String[] commands = ClientUtils.readFromStdin(true).split(
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
			Admin.exit(e.getMessage());
		}
	}
}
