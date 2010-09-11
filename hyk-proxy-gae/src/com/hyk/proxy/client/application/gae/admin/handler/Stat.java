/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Stat.java 
 *
 * @author qiying.wang [ Apr 19, 2010 | 2:01:50 PM ]
 *
 */
package com.hyk.proxy.client.application.gae.admin.handler;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.hyk.proxy.client.application.gae.admin.Admin;
import com.hyk.proxy.common.gae.auth.User;
import com.hyk.proxy.common.gae.stat.BandwidthStatisticsResult;
import com.hyk.proxy.common.rpc.service.BandwidthStatisticsService;
import com.hyk.proxy.common.rpc.service.RemoteServiceManager;

/**
 *
 */
public class Stat implements CommandHandler
{
	public static final String COMMAND = "stat";
	
	private static final String ON = "on";
	private static final String OFF = "off";
	private static final String STATUS = "status";
	private static final String CLEAR = "clear";
	private static final String REPORTS = "reports";
	private static final String LIMIT = " <limit>";
	
	private static final String SUB_COMMANDS = "Available subcommands:" + System.getProperty("line.separator")
	                                           + "       " + ON + System.getProperty("line.separator")
	                                           + "       " + OFF + System.getProperty("line.separator")
	                                           + "       " + CLEAR + System.getProperty("line.separator")
	                                           + "       " + STATUS + System.getProperty("line.separator")
	                                           + "       " + REPORTS + LIMIT + System.getProperty("line.separator");
	
	private static final String	EXAMPLE	= "Examples:"
		+ System.getProperty("line.separator")
		+ "stat on       # Enable bandwidth statistics service"
		+ System.getProperty("line.separator")
		+ "stat off      # Disable bandwidth statistics service"
		+ System.getProperty("line.separator")
		+ "stat reports 10 # Print the top 10 outgoing bandwidth statistics report"
		+ System.getProperty("line.separator");
	
	private RemoteServiceManager remoteServiceManager;
	private User userInfo;
	
	private Options				options	= new Options();
	
	public Stat(RemoteServiceManager remoteServiceManager, User userInfo)
	{
		this.remoteServiceManager = remoteServiceManager;
		this.userInfo = userInfo;
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
				BandwidthStatisticsService service = remoteServiceManager.getBandwidthStatisticsService(userInfo);
				String[] subcommandArgs = line.getArgs();
				if(subcommandArgs != null && subcommandArgs.length < 1)
				{
					Admin.outputln("Argument subcommand required!");
					printHelp();
				}
				else
				{
					String subcommand = subcommandArgs[0].trim();
					if(subcommand.equals(ON))
					{
						service.enable(true);
					}
					else if(subcommand.equals(OFF))
					{
						service.enable(false);
					}
					else if(subcommand.equals(STATUS))
					{
						Admin.outputln("Bandwidth statistics service is " + (service.isEnable()?ON:OFF));
					}
					else if(subcommand.equals(CLEAR))
					{
						service.clear();
					}
					else if(subcommand.equals(REPORTS))
					{
						if(subcommandArgs.length != 2)
						{
							Admin.outputln("Argument limit required for 'reports' !");
							printHelp();
							return;
						}
						int limit = Integer.parseInt(subcommandArgs[1].trim());
						List<BandwidthStatisticsResult> reports = service.getStatResults(limit);
						if(null != reports)
						{
							String formater = "%20s%20s%20s";
							String header = String.format(formater, "Host", "Incoming", "Outgoing");
							Admin.outputln(header);	
							for(BandwidthStatisticsResult report:reports)
							{
								String output = String.format(formater, report.getTargetSiteHost(), "" + report.getIncoming(), "" + report.getOutgoing());
								Admin.outputln(output);
							}
						}
						else
						{
							Admin.outputln("No report!");
						}
					}
					else
					{
						Admin.outputln("Error subcommand!");
						printHelp();
					}
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
		formatter.printHelp(COMMAND + " <subcommand>", "", options, SUB_COMMANDS + EXAMPLE);
	}

}
