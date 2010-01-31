/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: XmppAccount.java 
 *
 * @author Administrator [ 2010-1-31 | am10:50:02 ]
 *
 */
package com.hyk.proxy.gae.client.config;

/**
 *
 */
public class XmppAccount
{
	public String getName()
	{
		return name;
	}

	public String getPasswd()
	{
		return passwd;
	}

	public XmppAccount(String name, String passwd)
	{
		super();
		this.name = name;
		this.passwd = passwd;
	}

	private String	name;
	private String	passwd;

}
