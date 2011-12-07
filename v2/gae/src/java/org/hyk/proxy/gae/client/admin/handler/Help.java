/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Help.java 
 *
 * @author yinqiwen [ 2010-4-9 | 08:50:39 PM]
 *
 */
package org.hyk.proxy.gae.client.admin.handler;

import org.hyk.proxy.gae.client.admin.GAEAdmin;


/**
 *
 */
public class Help implements CommandHandler
{
	public static final String COMMAND = "help";
	
	public static final String USAGE = "   -- To clean screen use \"clear\" command." + System.getProperty("line.separator") + 
                                         "   -- To list all users use \"users\" command." + System.getProperty("line.separator") + 
                                         "   -- To list all groups use \"groups\" command." + System.getProperty("line.separator") + 
                                         "   -- To create user use \"useradd\" command." + System.getProperty("line.separator") + 
                                         "   -- To delete user use \"userdel\" command. " + System.getProperty("line.separator") + 
                                         "   -- To create group use \"groupadd\" command." + System.getProperty("line.separator") + 
                                         "   -- To delete group use \"groupdel\" command." + System.getProperty("line.separator") + 
                                         "   -- To change password use \"passwd\" command." + System.getProperty("line.separator") + 
                                         "   -- To config server use \"servercfg\" command." + System.getProperty("line.separator") + 
//                                         "   -- To control the traffic for a user use \"traffic\" command." + System.getProperty("line.separator") + 
                                         "   -- To print help use \"help\" command." + System.getProperty("line.separator") + 
                                         "   -- To add/delete web site to blacklist use \"blacklist\" command." + System.getProperty("line.separator") + 
                                         "   -- To exit use \"exit\" command." +  System.getProperty("line.separator") + 
                                         "   -- To get information about most common admin usage, please use help command." + System.getProperty("line.separator") + 
                                         "   -- If you have any questions, please visit to hyk-proxy website http://hyk-proxy.googlecode.com/";
	   
	@Override
	public void execute(String[] args)
	{
		GAEAdmin.outputln(USAGE);

	}
	@Override
	public void printHelp()
	{
		// TODO Auto-generated method stub
		
	}

}
