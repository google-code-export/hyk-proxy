/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ListUsers.java 
 *
 * @author yinqiwen [ 2010-4-9 | ÏÂÎç08:49:51 ]
 *
 */
package com.hyk.proxy.gae.client.admin.handler;

import java.util.List;

import com.hyk.proxy.gae.client.admin.Admin;
import com.hyk.proxy.gae.common.service.AccountService;

/**
 *
 */
public class ListGroups implements CommandHandler
{
	public static final String COMMAND = "groups";
	
	private AccountService		accountService;

	public ListGroups(AccountService accountService)
	{
		this.accountService = accountService;
		//options.addOption("h", "help", false, "print this message.");
	}
	
	@Override
	public void execute(String[] args)
	{
		List<String[]> retValue = accountService.getGroupsInfo();
		String formater = "%20s%20s";
		String header = String.format(formater,  "Group", "Blacklist");
		Admin.outputln(header);
		for(String[] line:retValue)
		{
			String output = String.format(formater, line[0], line[1]);
			Admin.outputln(output);
		}
	}

	@Override
	public void printHelp()
	{
		// TODO Auto-generated method stub
		
	}

}
