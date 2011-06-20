/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteObjectIdGenerator.java 
 *
 * @author yinqiwen [ 2010-4-18 | 08:24:42 PM ]
 *
 */
package com.hyk.rpc.core.remote;

import com.hyk.rpc.core.util.ID;

/**
 *
 */
public interface RemoteObjectIdGenerator
{
	public long generateRemoteObjectID();
	
	static RemoteObjectIdGenerator defaultGenerator = new RemoteObjectIdGenerator()
	{
		@Override
		public long generateRemoteObjectID()
		{
			return ID.generateRemoteObjectID();
		}
	};
}
