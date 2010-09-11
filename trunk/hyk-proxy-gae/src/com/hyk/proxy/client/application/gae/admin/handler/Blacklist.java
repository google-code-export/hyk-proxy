/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Blacklist.java 
 *
 * @author yinqiwen [ 2010-4-10 | 08:12:30 PM]
 *
 */
package com.hyk.proxy.client.application.gae.admin.handler;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.hyk.proxy.client.application.gae.admin.Admin;
import com.hyk.proxy.common.gae.auth.Operation;
import com.hyk.proxy.common.rpc.service.AccountService;


/**
 *
 */
public class Blacklist implements CommandHandler
{
	public static final String	COMMAND	= "blacklist";

	private static final String	EXAMPLE	= "Examples:"
												+ System.getProperty("line.separator")
												+ "blacklist -u anonymouse --add www.youtube.com  # Add 'www.youtube.com' in user anonymouse's blacklist."
												+ System.getProperty("line.separator")
												+ "blacklist -g anonymouse --delete www.youtube.com  # remove 'www.youtube.com' from group anonymouse's blacklist.."
												+ System.getProperty("line.separator");
	private Options				options	= new Options();

	private AccountService		accountService;

	public Blacklist(AccountService accountService)
	{
		this.accountService = accountService;
		options.addOption("g", "group", true, "specify the group.");
		options.addOption("u", "user", true, "specify the user.");
		options.addOption("a", "add", true, "add host into black list.");
		options.addOption("d", "delete", true, "remove host from black list.");
		options.addOption("h", "help", false, "print this message.");
	}

	protected void printHelp(String message)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(COMMAND + " [OPTIONS]", message, options, EXAMPLE);
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
				printHelp("");
			}
			else
			{
				if((!line.hasOption("u") && !line.hasOption("g")) || (!line.hasOption("a") && !line.hasOption("d")))
				{
					printHelp("blacklist: You must specify one of the `-ug' options");
				}
				else
				{
					String host = null;
					Operation opr = Operation.ADD;
					if(line.hasOption("a"))
					{
						host = line.getOptionValue("a");
						opr = Operation.ADD;
					}
					else if(line.hasOption("d"))
					{
						host = line.getOptionValue("d");
						opr = Operation.DELETE;
					}
					else
					{
						printHelp("Argument host must start with '+' or '-'.");
						return;
					}
					String result = null;
					if(line.hasOption("u"))
					{
						String username = line.getOptionValue("u");
						result = accountService.operationOnUserBlackList(username, host, opr);
					}
					else if(line.hasOption("g"))
					{
						String groupname = line.getOptionValue("g");
						result = accountService.operationOnGroupBlackList(groupname, host, opr);
					}
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
		printHelp("");
	}
}
