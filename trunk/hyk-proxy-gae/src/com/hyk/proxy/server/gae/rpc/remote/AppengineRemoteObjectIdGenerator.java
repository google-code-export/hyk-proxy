/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppengineRemoteObjectIdGenerator.java 
 *
 * @author yinqiwen [ 2010-4-18 | ÏÂÎç08:31:15 ]
 *
 */
package com.hyk.proxy.server.gae.rpc.remote;


import com.hyk.proxy.server.gae.util.ServerUtils;
import com.hyk.rpc.core.remote.RemoteObjectIdGenerator;

/**
 *
 */
public class AppengineRemoteObjectIdGenerator implements RemoteObjectIdGenerator
{
	//protected  Logger	logger = LoggerFactory.getLogger(getClass());
	@Override
	public long generateRemoteObjectID()
	{
		//Exception e = new Exception();
		//logger.error("Store remote object!", e);
		return ServerUtils.storeObject(new RemoteObjectId());
	}
}
