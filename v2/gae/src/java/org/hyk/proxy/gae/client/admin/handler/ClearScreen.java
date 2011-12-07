/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ClearScreenHandler.java 
 *
 * @author yinqiwen [ 2010-4-9 | 08:34:45 PM]
 *
 */
package org.hyk.proxy.gae.client.admin.handler;

import org.hyk.proxy.gae.client.admin.GAEAdmin;



/**
 *
 */
public class ClearScreen implements CommandHandler
{
	public static final String COMMAND = "clear";
	
	private String clrstr;
	
	public ClearScreen()
	{
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < 200; i++)
		{
			buffer.append(System.getProperty("line.separator"));
		}
		clrstr = buffer.toString();
	}
	
	@Override
	public void execute(String[] args)
	{
		GAEAdmin.outputln(clrstr);
	}

	@Override
	public void printHelp()
	{
		//Admin.outputln("");
	}

}
