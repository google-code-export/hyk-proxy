/**
 * 
 */
package com.hyk.rpc.core.transport.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.rpc.core.message.MessageFragment;
import com.hyk.rpc.core.message.MessageID;
import com.hyk.rpc.core.transport.RpcChannel;

/**
 * @author Administrator
 *
 */
public abstract class AbstractDefaultRpcChannel extends RpcChannel {

	protected  Logger			logger			= LoggerFactory.getLogger(getClass());
	
	protected Map<MessageID, MessageFragment[]> fragmentTable = new ConcurrentHashMap<MessageID, MessageFragment[]>();
	
	public AbstractDefaultRpcChannel(Executor threadPool) 
	{
		super(threadPool);
	}

	@Override
	protected void deleteMessageFragments(MessageID id) 
	{
		MessageFragment[] frags = fragmentTable.remove(id);
//		if(null != frags)
//		{
//			for(MessageFragment frag:frags)
//			{
//				if(null != frag)
//				{
//					//frag.getContent().free();
//				}
//			}
//		}
	}


	@Override
	protected MessageFragment[] loadMessageFragments(MessageID id) {
		return fragmentTable.get(id);
	}

	@Override
	protected void saveMessageFragment(MessageFragment fragment) {
		MessageFragment[] fragments = fragmentTable.get(fragment.getId());
		if(null == fragments)
		{
			fragments = new MessageFragment[fragment.getTotalFragmentCount()];
			fragmentTable.put(fragment.getId(), fragments);
		}
		if(null == fragments[fragment.getSequence()])
		{
			fragments[fragment.getSequence()] = fragment;
		}
		else
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Discard duplicate message fragment!");
			}
		}
		

	}

}
