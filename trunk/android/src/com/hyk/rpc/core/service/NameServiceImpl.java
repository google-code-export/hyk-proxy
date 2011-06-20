/**
 * 
 */
package com.hyk.rpc.core.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hyk.rpc.core.remote.RemoteObjectFactory;
import com.hyk.rpc.core.util.ID;

/**
 * @author Administrator
 * 
 */
public class NameServiceImpl implements NameService
{
	private RemoteObjectFactory	remoteObjectFactory;
	private Map<String, Object>	objTable	= new ConcurrentHashMap<String, Object>();

	public NameServiceImpl(RemoteObjectFactory remoteObjectFactory)
	{
		remoteObjectFactory.publish(this, ID.NAMING_ID);
		this.remoteObjectFactory = remoteObjectFactory;
	}

	@Override
	public Object lookup(String name)
	{
		return objTable.get(name);
	}

	@Override
	public void unbind(String name)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean bind(String name, Object obj)
	{
		if(objTable.containsKey(name))
		{
			return false;
		}
		objTable.put(name, remoteObjectFactory.publish(obj));
		return true;
	}

}
