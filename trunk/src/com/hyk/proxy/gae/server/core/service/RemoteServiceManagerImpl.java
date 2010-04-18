/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteServiceManagerImpl.java 
 *
 * @author yinqiwen [ 2010-4-7 | ÏÂÎç09:10:43 ]
 *
 */
package com.hyk.proxy.gae.server.core.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.common.auth.UserInfo;
import com.hyk.proxy.gae.common.service.AccountService;
import com.hyk.proxy.gae.common.service.AuthRuntimeException;
import com.hyk.proxy.gae.common.service.FetchService;
import com.hyk.proxy.gae.common.service.RemoteServiceManager;
import com.hyk.proxy.gae.server.account.Group;
import com.hyk.proxy.gae.server.account.User;
import com.hyk.proxy.gae.server.remote.RemoteObject;
import com.hyk.proxy.gae.server.remote.RemoteObjectType;
import com.hyk.proxy.gae.server.util.ServerUtils;
import com.hyk.rpc.core.RPC;

/**
 *
 */
public class RemoteServiceManagerImpl implements RemoteServiceManager
{
	protected Logger					logger				= LoggerFactory.getLogger(getClass());
	private RPC							rpc;

	private Map<String, FetchService>	fetchServiceTable	= new HashMap<String, FetchService>();
	private Map<String, AccountService>	accountServiceTable	= new HashMap<String, AccountService>();

	public RemoteServiceManagerImpl(RPC rpc)
	{
		this.rpc = rpc;
		loadRemoteObjects();
	}

	protected void saveRemoteObject(Object remoteObj, RemoteObjectType type, String username, String groupname)
	{
		long objid = rpc.getRemoteObjectId(remoteObj);
		ServerUtils.saveRemoteObject(objid, type, username, groupname);
	}

	protected void loadRemoteObjects()
	{
		List<RemoteObject> ros = ServerUtils.loadRemoteObjects();
		if(null == ros)
		{
			return;
		}
		for(RemoteObject ro : ros)
		{
			switch(ro.getType())
			{
				case FETCH:
				{
					FetchServiceImpl impl = createFetchServiceImpl(ro.getUsername(), ro.getGroupname());
					fetchServiceTable.put(ro.getUsername(), (FetchService)rpc.exportRemoteObject(impl, ro.getObjid()));
					break;
				}
				case ACCOUNT:
				{
					AccountServiceImpl impl = createAccountServiceImpl(ro.getUsername(), ro.getGroupname());
					accountServiceTable.put(ro.getUsername(), (AccountService)rpc.exportRemoteObject(impl, ro.getObjid()));
					break;
				}
			}
		}
	}

	protected FetchServiceImpl createFetchServiceImpl(String username, String groupname)
	{
		User user = ServerUtils.getUser(username);
		if(null == user)
		{
			logger.error("Invalid username.");
			throw new AuthRuntimeException("Invalid username.");
		}
		Group group = ServerUtils.getGroup(groupname);
		return new FetchServiceImpl(group, user);
	}

	protected AccountServiceImpl createAccountServiceImpl(String username, String groupname)
	{
		User user = ServerUtils.getUser(username);
		if(null == user)
		{
			logger.error("Invalid username.");
			throw new AuthRuntimeException("Invalid username.");
		}
		Group group = ServerUtils.getGroup(groupname);
		return new AccountServiceImpl(group, user);
	}

	@Override
	public FetchService getFetchService(UserInfo userInfo)
	{
		User user = ServerUtils.getUser(userInfo.getEmail());
		if(null == user || !user.getPasswd().equals(userInfo.getPasswd()))
		{
			logger.error("Invalid username/passwd.");
			throw new AuthRuntimeException("Invalid username/passwd.");
		}
		// Group group = ServerUtils.getGroup(user.getGroup());
		FetchService remoteService = null;
		if(!fetchServiceTable.containsKey(user.getEmail()))
		{
			FetchServiceImpl service = createFetchServiceImpl(user.getEmail(), user.getGroup());
			remoteService = (FetchService)rpc.exportRemoteObject(service);
			fetchServiceTable.put(user.getEmail(), remoteService);
			saveRemoteObject(remoteService, RemoteObjectType.FETCH, user.getEmail(), user.getGroup());
		}
		else
		{
			Group group = ServerUtils.getGroup(user.getGroup());
			remoteService = fetchServiceTable.get(user.getEmail());
			FetchServiceImpl service = (FetchServiceImpl)rpc.exportRawObject(remoteService);
			service.setUserAndGroup(group, user);
		}
		return remoteService;

	}

	@Override
	public AccountService getAccountService(UserInfo userInfo)
	{
		User user = ServerUtils.getUser(userInfo.getEmail());
		if(null == user || !user.getPasswd().equals(userInfo.getPasswd()))
		{
			logger.error("Invalid username/passwd.");
			throw new AuthRuntimeException("Invalid username/passwd.");
		}
		try
		{
			AccountService remoteService = null;
			if(!accountServiceTable.containsKey(user.getEmail()))
			{
				AccountServiceImpl service = createAccountServiceImpl(user.getEmail(), user.getGroup());
				remoteService = (AccountService)rpc.exportRemoteObject(service);
				accountServiceTable.put(user.getEmail(), remoteService);
				saveRemoteObject(remoteService, RemoteObjectType.ACCOUNT, user.getEmail(), user.getGroup());
			}
			else
			{
				Group group = ServerUtils.getGroup(user.getGroup());
				remoteService = accountServiceTable.get(user.getEmail());
				AccountServiceImpl service = (AccountServiceImpl)rpc.exportRawObject(remoteService);
				service.setUserAndGroup(group, user);
			}
			return remoteService;
		}
		catch(Throwable e)
		{
			logger.error("Failed to get account service", e);
			return null;
		}
	}

}
