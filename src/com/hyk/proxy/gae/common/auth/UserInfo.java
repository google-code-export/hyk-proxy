/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: UserInfo.java 
 *
 * @author yinqiwen [ 2010-4-7 | 09:04:45 PM]
 *
 */
package com.hyk.proxy.gae.common.auth;

import java.io.IOException;

import com.hyk.serializer.Externalizable;
import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 *
 */
public class UserInfo implements Externalizable
{
	private String email;
	private String passwd;
	
	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPasswd()
	{
		return passwd;
	}

	public void setPasswd(String passwd)
	{
		this.passwd = passwd;
	}
	
	@Override
	public void readExternal(SerializerInput in) throws IOException
	{
		email = in.readString();
		passwd = in.readString();
	}

	@Override
	public void writeExternal(SerializerOutput out) throws IOException
	{
		out.writeString(email);
		out.writeString(passwd);
	}

}
