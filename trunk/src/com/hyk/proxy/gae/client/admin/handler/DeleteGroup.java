/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AddGroup.java 
 *
 * @author yinqiwen [ 2010-4-9 | 10:23:41 PM]
 *
 */
package com.hyk.proxy.gae.client.admin.handler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.hyk.proxy.gae.client.admin.Admin;
import com.hyk.proxy.gae.common.service.AccountService;
import com.hyk.util.random.RandomUtil;

/**
 *
 */
public class DeleteGroup implements CommandHandler
{
	
	public static final String	COMMAND	= "groupdel";
	private Options				options	= new Options();
	
	private AccountService		accountService;

	public DeleteGroup(AccountService accountService)
	{
		this.accountService = accountService;
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
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(COMMAND + " groupname", options);
			}
			else
			{
				String[] groupnameargs = line.getArgs();
				if(groupnameargs != null && groupnameargs.length != 1)
				{
					Admin.outputln("Argument groupname required!");
				}
				String result = accountService.deleteGroup(groupnameargs[0]);
				Admin.outputln(result);
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
		// TODO Auto-generated method stub
		
	}

}
