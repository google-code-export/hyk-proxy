/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteObject.java 
 *
 * @author yinqiwen [ 2010-4-11 | ÉÏÎç11:11:23 ]
 *
 */
package com.hyk.proxy.gae.server.remote;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 *
 */
@Cached
public class RemoteObject
{
	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public long getObjid()
	{
		return objid;
	}
	
	public void setObjid(long objid)
	{
		this.objid = objid;
	}

	public RemoteObjectType getType()
	{
		return type;
	}

	public void setType(RemoteObjectType type)
	{
		this.type = type;
	}
	
	private String username;
	
	private String groupname;
	
	public String getGroupname()
	{
		return groupname;
	}

	public void setGroupname(String groupname)
	{
		this.groupname = groupname;
	}
    
    @Id
    private long objid;

	private RemoteObjectType type;
}
