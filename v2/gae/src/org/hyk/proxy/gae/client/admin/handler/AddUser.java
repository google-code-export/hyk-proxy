/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AddUser.java 
 *
 * @author yinqiwen [ 2010-4-9 | 10:24:50 PM ]
 *
 */
package org.hyk.proxy.gae.client.admin.handler;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.hyk.proxy.gae.client.admin.GAEAdmin;
import org.hyk.proxy.gae.client.admin.handler.CommandHandler.AdminResponseEventHandler;
import org.hyk.proxy.gae.client.connection.ProxyConnection;
import org.hyk.proxy.gae.common.auth.Operation;
import org.hyk.proxy.gae.common.auth.User;
import org.hyk.proxy.gae.common.event.UserOperationEvent;

/**
 *
 */
public class AddUser implements CommandHandler
{
	public static final String	COMMAND	= "useradd";
	private Options				options	= new Options();
	

	private ProxyConnection connection;
	public AddUser(ProxyConnection connection)
	{
		this.connection = connection;
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
					GAEAdmin.outputln("Argument username required!");
				}
				else
				{
					String group = "public";
					if(line.hasOption("g"))
					{
						group = line.getOptionValue("g");
					}
					UserOperationEvent event = new UserOperationEvent();
					event.opr = Operation.ADD;
					User user = new User();
					user.setEmail(usernameargs[0]);
					user.setGroup(group);
					event.user = user;
					AdminResponseEventHandler.syncSendEvent(connection, event);
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
