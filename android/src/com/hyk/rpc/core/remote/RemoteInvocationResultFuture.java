/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteInvocationResultFuture.java 
 *
 * @author qiying.wang [ Mar 2, 2010 | 3:34:30 PM ]
 *
 */
package com.hyk.rpc.core.remote;

import com.hyk.rpc.core.ResponseListener;
import com.hyk.rpc.core.Rpctimeout;
import com.hyk.rpc.core.message.Response;
import com.hyk.rpc.core.session.Session;

/**
 *
 */
public class RemoteInvocationResultFuture implements ResponseListener
{
	private Session session;
	private Object reply;
	private boolean hasRecvReply = false;
	
	protected Object getReplyObject() throws Throwable
	{
		if(reply != null && reply instanceof Throwable)
		{
			throw (Throwable)reply;
		}
		return reply;
	}
	

	public Object get() throws Throwable
	{
		return get(0);
	}

	public Object get(long timeout) throws Throwable
	{
		if(hasRecvReply)
		{
			return getReplyObject();
		}
		try
		{
			synchronized(this)
			{
				try
				{
					this.wait(timeout);
				}
				catch(InterruptedException e)
				{
					throw new Rpctimeout(e.getMessage());
				}
			}
			if(!hasRecvReply)
			{
				throw new Rpctimeout("RPC timeout!");
			}
			return getReplyObject();
		}
		finally
		{
			if(null != session)
			{
				session.close();
			}
		}
		
	}

	public boolean isDone()
	{
		return hasRecvReply;
	}

	@Override
	public void processResponse(Session session, Response res) throws Exception
	{
		reply = res.getReply();
		hasRecvReply = true;
		synchronized(this)
		{
			this.notify();
		}
	}


}
