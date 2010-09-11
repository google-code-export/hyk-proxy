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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Serialized;
import com.hyk.proxy.server.gae.util.ServerUtils;
import com.hyk.rpc.core.remote.RemoteObjectReference;

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
	
	//@NotSaved
	//static Serializer serializer = new StandardSerializer();
	
	public byte[] getRawData()
	{
		return rawData;
	}

	public RemoteObjectReference getRemoteRef()
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(rawData));
			//ChannelDataBuffer data = ChannelDataBuffer.wrap(rawData);
			RemoteObjectReference ref =  (RemoteObjectReference) ois.readObject();
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
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(ref);
			oos.close();
			//ChannelDataBuffer data = serializer.serialize(ref);
			rawData = bos.toByteArray();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
