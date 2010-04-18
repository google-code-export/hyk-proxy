/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppengineRemoteObjectIdGenerator.java 
 *
 * @author yinqiwen [ 2010-4-18 | обнГ08:31:15 ]
 *
 */
package com.hyk.proxy.gae.server.remote;

import com.hyk.proxy.gae.server.util.ServerUtils;
import com.hyk.rpc.core.remote.RemoteObjectIdGenerator;

/**
 *
 */
public class AppengineRemoteObjectIdGenerator implements RemoteObjectIdGenerator
{
	@Override
	public long generateRemoteObjectID()
	{
		return ServerUtils.storeObject(new RemoteObjectId());
	}
}
