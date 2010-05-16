/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Group.java 
 *
 * @author yinqiwen [ 2010-4-7 | ÏÂÎç09:09:01 ]
 *
 */
package com.hyk.proxy.common.gae.auth;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 *
 */
@Cached
public class Group implements Serializable
{
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Set<String> getBlacklist()
	{
		return blacklist;
	}

	public void setBlacklist(Set<String> blacklist)
	{
		this.blacklist = blacklist;
	}

	@Id
	private String name;

	private Set<String> blacklist;
	
}
