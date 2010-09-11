/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppEngineRemoteObjectStorage.java 
 *
 * @author yinqiwen [ 2010-5-15 | ÏÂÎç01:47:45 ]
 *
 */
package com.hyk.proxy.server.gae.rpc.remote;

import java.util.ArrayList;
import java.util.List;

import com.hyk.proxy.server.gae.util.ServerUtils;
import com.hyk.rpc.core.remote.RemoteObjectReference;
import com.hyk.rpc.core.remote.RemoteObjectStorage;

/**
 *
 */
public class AppEngineRemoteObjectStorage implements RemoteObjectStorage
{
	@Override
	public List<RemoteObjectReference> loadAll()
	{
		List<RemoteObject> ros = ServerUtils.loadRemoteObjects();
		List<RemoteObjectReference> ret = new ArrayList<RemoteObjectReference>();
		for(RemoteObject ro:ros)
		{
			if(null == ro.getRemoteRef())
			{
				continue;
			}
			Object impl = ro.getRemoteRef().getImpl();
			if(impl instanceof Reloadable)
			{
				((Reloadable)impl).init();
			}
			ret.add(ro.getRemoteRef());
		}
		return ret;
	}

	@Override
	public void store(RemoteObjectReference ref)
	{
		RemoteObject ro = new RemoteObject();
		ro.id = ref.getObjID();
		ro.setRemoteRef(ref);
		ServerUtils.storeObject(ro);
	}

}
