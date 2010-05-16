/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Exit.java 
 *
 * @author yinqiwen [ 2010-4-9 | 10:19:07 PM]
 *
 */
package com.hyk.proxy.client.gae.admin.handler;

/**
 *
 */
public class Exit implements CommandHandler
{
	public static final String COMMAND = "exit";
	
	@Override
	public void execute(String[] args)
	{
		System.exit(1);
	}

	@Override
	public void printHelp()
	{
		// TODO Auto-generated method stub
		
	}

}
