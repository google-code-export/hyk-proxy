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

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class RemoteObject implements Serializable
{
	public static final String CACHE_LIST_NAME = "RemoteObjectList";
	
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

	@Persistent
	private String username;
	
	@Persistent
	private String groupname;
	
	public String getGroupname()
	{
		return groupname;
	}

	public void setGroupname(String groupname)
	{
		this.groupname = groupname;
	}

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;
	
	@Persistent
	private long objid;
	
	@Persistent
	private RemoteObjectType type;
}
