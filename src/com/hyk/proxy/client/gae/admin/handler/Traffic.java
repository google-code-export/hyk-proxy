/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Blacklist.java 
 *
 * @author yinqiwen [ 2010-4-10 | 08:12:30 PM]
 *
 */
package com.hyk.proxy.client.gae.admin.handler;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.hyk.proxy.client.gae.admin.Admin;
import com.hyk.proxy.common.rpc.service.AccountService;

/**
 *
 */
public class Traffic implements CommandHandler
{
	public static final String	COMMAND	= "traffic";

	private static final String	EXAMPLE	= "Examples:"
												+ System.getProperty("line.separator")
												+ "traffic -u anonymouse -s * -r 20000000 # Restrict 20000000 bytes traffic for all sites."
												+ System.getProperty("line.separator");
	private Options				options	= new Options();

	private AccountService		accountService;

	public Traffic(AccountService accountService)
	{
		this.accountService = accountService;
		options.addOption("u", "user", true, "specify the user.");
		options.addOption("s", "site", true, "specify the remote host site for traffic restriction.");
		options.addOption("r", "restriction", true, "specify the restriction");
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
				if(!line.hasOption("u") || !line.hasOption("s") || !line.hasOption("r"))
				{
					printHelp("blacklist: You must specify all of the `-u -s -r' options");
				}
				else
				{
					String user = line.getOptionValue("u");
					String site = line.getOptionValue("s");
					int restriction = -1;
					try
					{
						String restrictionStr = line.getOptionValue("r");
						restriction = Integer.parseInt(restrictionStr);
					}
					catch(NumberFormatException e)
					{
						Admin.outputln("Restriction option value MUST be a valid int value.");
						return;
					}
					
					String result = accountService.operationOnUserTraffic(user, site, restriction);				
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
