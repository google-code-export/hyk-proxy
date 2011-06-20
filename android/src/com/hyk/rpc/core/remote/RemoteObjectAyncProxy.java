/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteObjectAyncProxy.java 
 *
 * @author qiying.wang [ Mar 2, 2010 | 4:14:51 PM ]
 *
 */
package com.hyk.rpc.core.remote;

import java.lang.reflect.Method;

import com.hyk.rpc.core.ResponseListener;
import com.hyk.rpc.core.RpcCallback;
import com.hyk.rpc.core.RpcCallbackResult;
import com.hyk.rpc.core.message.Response;
import com.hyk.rpc.core.session.Session;

/**
 *
 */
public class RemoteObjectAyncProxy extends RemoteObjectProxy
{
	public RemoteObjectAyncProxy(RemoteObjectProxy proxy)
	{
		this.objID = proxy.objID;
		hostAddress = proxy.hostAddress;
		sessionManager = proxy.sessionManager;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		Object[] invokeArgs = args;
		RpcCallback callback = null;
		if(args.length > 0 && (args[args.length - 1] instanceof RpcCallback))
		{
			callback = (RpcCallback)args[args.length - 1];
			invokeArgs = new Object[args.length - 1];
			System.arraycopy(args, 0, invokeArgs, 0, args.length - 1);	
		}
		ResponseListener listener = null;
		if(null != callback)
		{
			final RpcCallback temp = callback;
			listener = new ResponseListener()
			{
				@Override
				public void processResponse(Session session, Response res) throws Exception
				{
					Object reply = res.getReply();
					Throwable e = null;
					if(reply != null && reply instanceof Throwable)
					{
						e = (Throwable)reply;
						reply = null;
					}
					temp.callBack(new RpcCallbackResult(reply, e));
				}	
			};
		}
		invokeWithResponseListener(method, invokeArgs, listener);
		return null;
	}
}
