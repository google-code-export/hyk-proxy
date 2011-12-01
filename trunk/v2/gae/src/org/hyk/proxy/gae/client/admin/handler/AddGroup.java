/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AddGroup.java 
 *
 * @author yinqiwen [ 2010-4-9 | 10:23:41 PM ]
 *
 */
package org.hyk.proxy.gae.client.admin.handler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.hyk.proxy.gae.client.admin.GAEAdmin;
import org.hyk.proxy.gae.client.connection.ProxyConnection;
import org.hyk.proxy.gae.common.auth.Group;
import org.hyk.proxy.gae.common.auth.Operation;
import org.hyk.proxy.gae.common.event.GroupOperationEvent;


/**
 *
 */
public class AddGroup implements CommandHandler
{
	
	public static final String	COMMAND	= "groupadd";
	private Options				options	= new Options();
	

	private ProxyConnection connection;
	public AddGroup(ProxyConnection connection)
	{
		this.connection = connection;
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
				String[] groupnameargs = line.getArgs();
				if(groupnameargs != null && groupnameargs.length != 1)
				{
					GAEAdmin.outputln("Argument groupname required!");
				}
				//String result = accountService.createGroup(groupnameargs[0]);
				GroupOperationEvent event = new GroupOperationEvent();
				event.opr = Operation.ADD;
				event.grp = new Group();
				event.grp.setName(groupnameargs[0]);
				AdminResponseEventHandler.syncSendEvent(connection, event);
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
		formatter.printHelp(COMMAND + " groupname", options);
	}

}
