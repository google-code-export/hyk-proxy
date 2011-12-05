/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ConfigServer.java 
 *
 * @author yinqiwen [ 2011-12-5 | ÏÂÎç10:44:57 ]
 *
 */
package org.hyk.proxy.gae.client.admin.handler;

import java.util.Arrays;

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
import org.hyk.proxy.gae.common.event.ServerConfigEvent;
import org.hyk.proxy.gae.common.event.UserOperationEvent;

/**
 *
 */
public class ConfigServer implements CommandHandler
{
	public static final String COMMAND = "servercfg";

	private static final String EXAMPLE = "Examples:"
	        + System.getProperty("line.separator")
	        + "servercfg get  #get server config  "
	        + System.getProperty("line.separator")
	        + "servercfg set  #set server config by gae-server.xml."
	        + System.getProperty("line.separator");
	private Options options = new Options();

	private ProxyConnection connection;

	public ConfigServer(ProxyConnection connection)
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
			if (line.hasOption("h"))
			{
				printHelp();
			}
			else
			{
				String[] oprargs = line.getArgs();
				if (oprargs != null && oprargs.length > 1)
				{
					System.out.println(oprargs.length);
					GAEAdmin.outputln("Only one arg expected!"
					        + Arrays.toString(oprargs));
				}
				else
				{
					String opr = "get";
					if (oprargs != null && oprargs.length == 1)
					{
						opr = oprargs[0];
					}
					opr = opr.toLowerCase();
					if(!opr.equals("get") && !opr.equals("set"))
					{
						GAEAdmin.errln("Only get/set parameter is valid");
						return;
					}
					
					ServerConfigEvent event = new ServerConfigEvent();
					if(opr.equals("get"))
					{
						event.opreration = ServerConfigEvent.GET_CONFIG_REQ;
					}
					else
					{
						event.opreration = ServerConfigEvent.SET_CONFIG_REQ;
						
					}
					AdminResponseEventHandler.syncSendEvent(connection, event);
				}

			}

		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			System.out.println("Error:" + exp.getMessage());
		}

	}

	@Override
	public void printHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(COMMAND + " [get|set]", options);

	}

}
