/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AddUser.java 
 *
 * @author yinqiwen [ 2010-4-9 | 10:24:50 PM ]
 *
 */
package com.hyk.proxy.client.application.gae.admin.handler;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.hyk.proxy.client.application.gae.admin.Admin;
import com.hyk.proxy.common.rpc.service.AccountService;
import com.hyk.util.random.RandomUtil;

/**
 *
 */
public class AddUser implements CommandHandler
{
	public static final String	COMMAND	= "useradd";
	private Options				options	= new Options();
	
	private AccountService		accountService;
	
	public AddUser(AccountService accountService)
	{
		this.accountService = accountService;
		options.addOption("g", "group", true, "specify the group.");
		options.addOption("h", "help", false, "print this message.");
	}
	
	@Override
	public void execute(String[] args)
	{
		CommandLineParser parser = new PosixParser();

		try
		{
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			// validate that block-size has been set
			if(line.hasOption("h"))
			{
				printHelp();
			}
			else
			{
				String[] usernameargs = line.getArgs();
				if(usernameargs != null && usernameargs.length != 1)
				{
					Admin.outputln("Argument username required!");
				}
				else
				{
					String group = "public";
					if(line.hasOption("g"))
					{
						group = line.getOptionValue("g");
					}
					String result = accountService.createUser(usernameargs[0], group, RandomUtil.generateRandomString(8));
					Admin.outputln(result);
				}
				
			}
		}
		catch(Exception exp)
		{
			System.out.println("Error:" + exp.getMessage());
		}

	}

	@Override
	public void printHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(COMMAND + " [OPTION] username", options);
	}

}
