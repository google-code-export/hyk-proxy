/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteServiceManager.java 
 *
 * @author yinqiwen [ 2010-4-7 | 09:02:33 PM]
 *
 */
package com.hyk.proxy.common.rpc.service;

import com.hyk.proxy.common.gae.auth.User;
import com.hyk.rpc.core.annotation.Remote;

/**
 *
 */
@Remote
public interface RemoteServiceManager
{
	public static final String NAME = "REMOTE_SERVICE_MANAGER";
	
	public FetchService getFetchService(User user);
	
	public String getServerVersion();
}
