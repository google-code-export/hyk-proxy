/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteServiceManager.java 
 *
 * @author yinqiwen [ 2010-4-7 | обнГ09:02:33 ]
 *
 */
package com.hyk.proxy.gae.common.service;

import com.hyk.proxy.gae.common.auth.UserInfo;
import com.hyk.rpc.core.annotation.Remote;

/**
 *
 */
@Remote
public interface RemoteServiceManager
{
	public static final String NAME = "REMOTE_SERVICE_MANAGER";
	
	public FetchService getFetchService(UserInfo user);
	
	public AccountService getAccountService(UserInfo user);
}
