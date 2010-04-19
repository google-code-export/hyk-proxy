/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AdminLauncher.java 
 *
 * @author yinqiwen [ 2010-4-9 | 07:15:54 PM]
 *
 */
package com.hyk.proxy.gae.client.launch.tui;

import java.io.IOException;

import com.hyk.proxy.gae.client.admin.Admin;

/**
 *
 */
public class AdminLauncher
{

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		try
		{
			new Admin().start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Admin.exit(e.getMessage());
		}
		
	}

}
