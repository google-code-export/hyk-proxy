/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ChangePasswd.java 
 *
 * @author yinqiwen [ 2010-4-9 | 08:48:34 PM]
 *
 */
package com.hyk.proxy.client.application.gae.admin.handler;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.hyk.proxy.client.application.gae.admin.Admin;
import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.common.rpc.service.AccountService;

/**
 *
 */
public class ChangePasswd implements CommandHandler
{
	public static final String	COMMAND	= "passwd";

	private Options				options	= new Options();

	private String				username;
	private AccountService		accountService;

	public ChangePasswd(String username, AccountService accountService)
	{
		this.username = username;
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
				printHelp();
			}
			else
			{
				String[] passwdargs = line.getArgs();
				if(passwdargs != null && passwdargs.length > 1)
				{
					System.out.println(passwdargs.length);
					Admin.outputln("Only one arg expected!" + Arrays.toString(passwdargs));
				}
				else
				{
					String modifyUser = username;
					if(passwdargs != null && passwdargs.length == 1)
					{
						modifyUser = passwdargs[0];
					}
					String oldpass = "";
					if(!username.equals("root"))
					{
						Admin.outputln("Enter the old password");
						Admin.output("Old password:");
						oldpass = ClientUtils.readFromStdin(false);
					}
					Admin.outputln("Enter the new password");
					Admin.output("New password:");
					String newpass = ClientUtils.readFromStdin(false);
					String result = accountService.modifyPassword(modifyUser, oldpass, newpass);
					Admin.outputln(result);
				}

			}

		}
		catch(Exception exp)
		{
			System.out.println("Error:" + exp.getMessage());
		}

		// formatter.p
	}

	@Override
	public void printHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(COMMAND + " [username]", options);
		
	}

}
