/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Authorization.java 
 *
 * @author yinqiwen [ 2010-4-6 | 09:18:10 PM ]
 *
 */
package org.hyk.proxy.gae.common.auth;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;

/**
 *
 */
public class User implements Serializable
{
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

	public Map<String, Integer> getTrafficRestrictionTable()
	{
		return trafficRestrictionTable;
	}

	public void setTrafficRestrictionTable(
	        Map<String, Integer> trafficRestrictionTable)
	{
		this.trafficRestrictionTable = trafficRestrictionTable;
	}

	public String getAuthToken()
	{
		return authToken;
	}

	public void setAuthToken(String authToken)
	{
		this.authToken = authToken;
	}

	private String email;

	private String passwd;

	private String group;

	private String authToken;

	private Set<String> blacklist;

	private Map<String, Integer> trafficRestrictionTable;
	
	public void encode(Buffer buffer)
	{
		BufferHelper.writeVarString(buffer, email);
		BufferHelper.writeVarString(buffer, passwd);
		BufferHelper.writeVarString(buffer, group);
		BufferHelper.writeVarString(buffer, authToken);
	}
	
	public void decode(Buffer buffer)
	{
		try
        {
	        email = BufferHelper.readVarString(buffer);
	        passwd = BufferHelper.readVarString(buffer);
			group = BufferHelper.readVarString(buffer);
			authToken = BufferHelper.readVarString(buffer);
        }
        catch (IOException e)
        {
	        
        }
		
	}

}
