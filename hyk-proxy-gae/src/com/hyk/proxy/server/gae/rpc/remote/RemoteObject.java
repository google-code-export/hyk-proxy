/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteObject.java 
 *
 * @author yinqiwen [ 2010-4-11 | ÉÏÎç11:11:23 ]
 *
 */
package com.hyk.proxy.server.gae.rpc.remote;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.NotSaved;
import com.googlecode.objectify.annotation.Serialized;
import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.proxy.server.gae.util.ServerUtils;
import com.hyk.rpc.core.remote.RemoteObjectReference;
import com.hyk.serializer.Serializer;
import com.hyk.serializer.StandardSerializer;

/**
 *
 */
@Cached
public class RemoteObject
{
	@Id
    Long id;
	
	@Serialized
	byte[] rawData;
	
	@NotSaved
	static Serializer serializer = new StandardSerializer();
	
	public byte[] getRawData()
	{
		return rawData;
	}

	public RemoteObjectReference getRemoteRef()
	{
		try
		{
			ChannelDataBuffer data = ChannelDataBuffer.wrap(rawData);
			RemoteObjectReference ref =  serializer.deserialize(RemoteObjectReference.class, data);
			Object impl = ref.getImpl();
			if(impl instanceof Reloadable)
			{
				((Reloadable)impl).init();
			}
			return ref;
		}
		catch(Exception e)
		{
			ServerUtils.ofy.delete(RemoteObjectId.class, id);
			ServerUtils.ofy.delete(RemoteObject.class, id);
		}
		return null;	
	}
	
	public void setRemoteRef(RemoteObjectReference ref)
	{
		try
		{
			ChannelDataBuffer data = serializer.serialize(ref);
			rawData = ChannelDataBuffer.asByteArray(data);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
