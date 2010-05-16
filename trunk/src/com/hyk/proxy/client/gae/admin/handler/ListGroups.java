/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ListUsers.java 
 *
 * @author yinqiwen [ 2010-4-9 | 08:49:51 PM]
 *
 */
package com.hyk.proxy.client.gae.admin.handler;

import java.util.List;

import com.hyk.proxy.client.gae.admin.Admin;
import com.hyk.proxy.common.gae.auth.Group;
import com.hyk.proxy.common.rpc.service.AccountService;

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
		List<Group> retValue = accountService.getGroupsInfo();
		String formater = "%20s%20s";
		String header = String.format(formater,  "Group", "Blacklist");
		Admin.outputln(header);
		for(Group line:retValue)
		{
			String output = String.format(formater, line.getName(), line.getBlacklist());
			Admin.outputln(output);
		}
	}

	@Override
	public void printHelp()
	{
		// TODO Auto-generated method stub
		
	}

}
