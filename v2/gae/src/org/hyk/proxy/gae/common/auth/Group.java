/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Group.java 
 *
 * @author yinqiwen [ 2010-4-7 | 09:09:01 PM ]
 *
 */
package org.hyk.proxy.gae.common.auth;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.buffer.CodecObject;


/**
 *
 */
public class Group implements CodecObject
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

	private String name;

	private Set<String> blacklist;
	
	public boolean encode(Buffer buffer)
	{
		BufferHelper.writeVarString(buffer, name);
		BufferHelper.writeSet(buffer, blacklist);
		return true;
	}
	
	public boolean decode(Buffer buffer)
	{
		try
        {
			name = BufferHelper.readVarString(buffer);
			blacklist = BufferHelper.readSet(buffer, String.class);
			return true;

        }
        catch (IOException e)
        {
	        return false;
        }
		
	}
	
}
