/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ListUsers.java 
 *
 * @author yinqiwen [ 2010-4-9 | 08:49:51 PM]
 *
 */
package com.hyk.proxy.client.application.gae.admin.handler;

import java.util.List;

import com.hyk.proxy.client.application.gae.admin.Admin;
import com.hyk.proxy.common.gae.auth.User;
import com.hyk.proxy.common.rpc.service.AccountService;

/**
 *
 */
public class ListUsers implements CommandHandler
{
	public static final String COMMAND = "users";
	
	private AccountService		accountService;

	public ListUsers(AccountService accountService)
	{
		this.accountService = accountService;
		//options.addOption("h", "help", false, "print this message.");
	}
	
	@Override
	public void execute(String[] args)
	{
		List<User> retValue = accountService.getUsersInfo();
		String formater = "%12s%12s%12s%24s%24s";
		String header = String.format(formater, "Username", "Password", "Group", "Blacklist", "TrafficRestrictions");
		Admin.outputln(header);
		for(User line:retValue)
		{
			String output = String.format(formater, line.getEmail(), line.getPasswd(), line.getGroup(), line.getBlacklist(), line.getTrafficRestrictionTable());
			Admin.outputln(output);
		}
	}

	@Override
	public void printHelp()
	{
		// TODO Auto-generated method stub
		
	}

}
