/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AsyncRemoteServiceManager.java 
 *
 * @author yinqiwen [ 2010-5-29 | 04:35:50 PM ]
 *
 */
package com.hyk.proxy.common.rpc.service;

import com.hyk.proxy.common.gae.auth.User;
import com.hyk.rpc.core.RpcCallback;
import com.hyk.rpc.core.annotation.Async;

/**
 *
 */
@Async(RemoteServiceManager.class)
public interface AsyncRemoteServiceManager
{
	public void getFetchService(User user, RpcCallback<FetchService> callback);


	public void getServerVersion(RpcCallback<String> callback);
}
