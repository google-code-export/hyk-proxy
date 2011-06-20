/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteObjectHolder.java 
 *
 * @author qiying.wang [ May 11, 2010 | 4:32:47 PM ]
 *
 */
package com.hyk.rpc.core.remote;

import java.io.Serializable;

import com.hyk.rpc.core.util.RemoteUtil;

/**
 *
 */
public class RemoteObjectReference implements Serializable
{
	private long objID;
	private Object impl;
	
	private RemoteObjectReference()
	{
		
	}
	
	public Object getImpl()
	{
		return impl;
	}
	
	public long getObjID()
	{
		return objID;
	}
	
	
	public boolean isSerializable()
	{
		return impl instanceof Serializable;
	}
	
	public static RemoteObjectReference refernce(long id, Object rawObj)
	{
		RemoteObjectReference reference = new RemoteObjectReference();
		reference.objID = id;
		reference.impl = rawObj;
		return reference;
	}
}
