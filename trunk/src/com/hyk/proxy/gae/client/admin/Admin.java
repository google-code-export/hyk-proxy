/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Admin.java 
 *
 * @author yinqiwen [ 2010-4-9 | ÏÂÎç06:59:44 ]
 *
 */
package com.hyk.proxy.gae.client.admin;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jivesoftware.smack.XMPPException;

import com.hyk.proxy.gae.client.admin.handler.AddGroup;
import com.hyk.proxy.gae.client.admin.handler.AddUser;
import com.hyk.proxy.gae.client.admin.handler.Blacklist;
import com.hyk.proxy.gae.client.admin.handler.ChangePasswd;
import com.hyk.proxy.gae.client.admin.handler.ClearScreen;
import com.hyk.proxy.gae.client.admin.handler.CommandHandler;
import com.hyk.proxy.gae.client.admin.handler.DeleteGroup;
import com.hyk.proxy.gae.client.admin.handler.DeleteUser;
import com.hyk.proxy.gae.client.admin.handler.Exit;
import com.hyk.proxy.gae.client.admin.handler.Help;
import com.hyk.proxy.gae.client.admin.handler.ListGroups;
import com.hyk.proxy.gae.client.admin.handler.ListUsers;
import com.hyk.proxy.gae.client.config.Config;
import com.hyk.proxy.gae.client.util.ClientUtils;
import com.hyk.proxy.gae.common.Version;
import com.hyk.proxy.gae.common.auth.UserInfo;
import com.hyk.proxy.gae.common.http.message.HttpServerAddress;
import com.hyk.proxy.gae.common.service.AccountService;
import com.hyk.proxy.gae.common.service.RemoteServiceManager;
import com.hyk.proxy.gae.common.xmpp.XmppAddress;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.RpcException;

/**
 *
 */
public class Admin
{
	private static final String			PROMOTE		= "$ ";

	private Map<String, CommandHandler>	handlers	= new HashMap<String, CommandHandler>();

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
		if(null != msg)
		{
			System.out.print(msg);
		}
	}

	public static void outputln(String msg)
	{
		if(null != msg)
		{
			System.out.println(msg);
		}
	}

	public static void exit(String msg)
	{
		errln(msg);
		System.exit(0);
	}

	protected void registerCommandHandler(String user, AccountService accountService)
	{
		handlers.put(Exit.COMMAND, new Exit());
		handlers.put(ClearScreen.COMMAND, new ClearScreen());
		handlers.put(ChangePasswd.COMMAND, new ChangePasswd(user, accountService));
		handlers.put(AddUser.COMMAND, new AddUser(accountService));
		handlers.put(AddGroup.COMMAND, new AddGroup(accountService));
		handlers.put(ListUsers.COMMAND, new ListUsers(accountService));
		handlers.put(ListGroups.COMMAND, new ListGroups(accountService));
		handlers.put(DeleteUser.COMMAND, new DeleteUser(accountService));
		handlers.put(DeleteGroup.COMMAND, new DeleteGroup(accountService));
		handlers.put(Blacklist.COMMAND, new Blacklist(accountService));
		handlers.put(Help.COMMAND, new Help());
	}

	public void start() throws IOException, RpcException, XMPPException
	{
		Config config = Config.getInstance();
		output("appid:");
		String appid = ClientUtils.readFromStdin(true);
		output("login:");
		String user = ClientUtils.readFromStdin(true);
		output("password:");
		String passwd = ClientUtils.readFromStdin(false);

		Executor executor = Executors.newCachedThreadPool();
		RemoteServiceManager remoteServiceManager = null;
		if(config.isHttpEnable())
		{
			RPC rpc = ClientUtils.createHttpRPC(appid, executor);
			remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME, new HttpServerAddress(appid
					+ ".appspot.com", "/fetchproxy"));
			//remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME, new HttpServerAddress("localhost",
			//		8888, "/fetchproxy"));
		}
		else
		{
			RPC rpc = ClientUtils.createXmppRPC(config.getAccounts().get(0), executor);
			remoteServiceManager = rpc.getRemoteService(RemoteServiceManager.class, RemoteServiceManager.NAME,
					new XmppAddress(appid + "@appspot.com"));
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setEmail(user);
		userInfo.setPasswd(passwd);
		AccountService accountService = null;
		try
		{
			accountService = remoteServiceManager.getAccountService(userInfo);
		}
		catch(Exception e)
		{
			// e.printStackTrace();
			exit(e.getMessage());
		}
		InputStream is = getClass().getResourceAsStream("welcome.txt");
		byte[] buffer = new byte[4096];
		int len = is.read(buffer);
		String format = new String(buffer, 0, len);
		outputln(String.format(format, Version.value, Help.USAGE, Version.value, Version.value));

		registerCommandHandler(user, accountService);

		while(true)
		{
			outputln(user + "@" + appid + " ~");
			output(PROMOTE);
			String[] commands = ClientUtils.readFromStdin(true).split("\\s+");
			if(null == commands || commands.length == 0 || commands[0].trim().equals(""))
			{
				continue;
			}
			String[] args = new String[commands.length - 1];
			System.arraycopy(commands, 1, args, 0, args.length);
			CommandHandler handler = handlers.get(commands[0].trim());
			if(null == handler)
			{
				outputln("-admin: " + commands[0] + ": command not found");
				continue;
			}
			handler.execute(args);
		}
	}
}
