/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: CommandHandler.java 
 *
 * @author yinqiwen [ 2010-4-9 | 08:29:43 PM]
 *
 */
package com.hyk.proxy.client.application.gae.admin.handler;

/**
 *
 */
public interface CommandHandler
{
	public void execute(String[] args);
	
	public void printHelp();
}
