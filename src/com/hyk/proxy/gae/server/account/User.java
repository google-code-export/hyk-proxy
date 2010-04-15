/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Authorization.java 
 *
 * @author yinqiwen [ 2010-4-6 | 09:18:10 PM ]
 *
 */
package com.hyk.proxy.gae.server.account;

import java.io.Serializable;
import java.util.Set;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class User implements Serializable
{
	public static final String CACHE_NAME = "CACHE_USER";
	
	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getGroup()
	{
		return group;
	}

	public void setGroup(String group)
	{
		this.group = group;
	}
	
	public String getPasswd()
	{
		return passwd;
	}

	public void setPasswd(String passwd)
	{
		this.passwd = passwd;
	}
	
	public Set<String> getBlacklist()
	{
		return blacklist;
	}

	public void setBlacklist(Set<String> blacklist)
	{
		this.blacklist = blacklist;
	}

	@PrimaryKey
	@Persistent
	private String email;
	
	@Persistent
	private String passwd;

	@Persistent
	private String group;
	
	@Persistent
	private Set<String> blacklist;
}
