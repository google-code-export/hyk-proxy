/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AbstractAppEngineRpcChannel.java 
 *
 * @author yinqiwen [ Jan 26, 2010 | 5:33:34 PM ]
 *
 */
package com.hyk.proxy.gae.server.core.rpc;

import java.io.IOException;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.hyk.rpc.core.message.MessageFragment;
import com.hyk.rpc.core.message.MessageID;
import com.hyk.rpc.core.transport.RpcChannel;
import com.hyk.rpc.core.transport.RpcChannelData;

/**
 *
 */
public abstract class AbstractAppEngineRpcChannel extends RpcChannel
{
	protected MemcacheService					memcache	= MemcacheServiceFactory.getMemcacheService();

	@Override
	protected void deleteMessageFragments(MessageID id)
	{
		memcache.delete(id);
	}

	@Override
	protected MessageFragment[] loadMessageFragments(MessageID id)
	{
		return (MessageFragment[])memcache.get(id);
	}

	@Override
	protected RpcChannelData read() throws IOException
	{
		throw new IOException("Not supported!");
	}

	@Override
	protected void saveMessageFragment(MessageFragment fragment)
	{
		MessageFragment[] fragments = (MessageFragment[])memcache.get(fragment.getId());
		if(null == fragments)
		{
			fragments = new MessageFragment[fragment.getTotalFragmentCount()];
		}
		fragments[fragment.getSequence()] = fragment;
		memcache.put(fragment.getId(), fragments);
	}
}
