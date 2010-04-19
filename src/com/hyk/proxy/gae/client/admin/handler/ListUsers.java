/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ListUsers.java 
 *
 * @author yinqiwen [ 2010-4-9 | 08:49:51 PM]
 *
 */
package com.hyk.proxy.gae.client.admin.handler;

import java.util.List;

import com.hyk.proxy.gae.client.admin.Admin;
import com.hyk.proxy.gae.common.service.AccountService;

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
		List<String[]> retValue = accountService.getUsersInfo();
		String formater = "%20s%20s%20s%20s";
		String header = String.format(formater, "Username", "Password", "Group", "Blacklist");
		Admin.outputln(header);
		for(String[] line:retValue)
		{
			String output = String.format(formater, line[0], line[1], line[2], line[3]);
			Admin.outputln(output);
		}
	}

	@Override
	public void printHelp()
	{
		// TODO Auto-generated method stub
		
	}

}
