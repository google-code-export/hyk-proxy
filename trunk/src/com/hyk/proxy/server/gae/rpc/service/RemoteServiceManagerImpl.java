/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteServiceManagerImpl.java 
 *
 * @author yinqiwen [ 2010-4-7 | ÏÂÎç09:10:43 ]
 *
 */
package com.hyk.proxy.server.gae.rpc.service;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.gae.auth.Group;
import com.hyk.proxy.common.gae.auth.User;
import com.hyk.proxy.common.rpc.service.AccountService;
import com.hyk.proxy.common.rpc.service.AuthRuntimeException;
import com.hyk.proxy.common.rpc.service.BandwidthStatisticsService;
import com.hyk.proxy.common.rpc.service.FetchService;
import com.hyk.proxy.common.rpc.service.RemoteServiceManager;
import com.hyk.proxy.server.gae.rpc.remote.RemoteObject;
import com.hyk.proxy.server.gae.util.ServerUtils;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.remote.RemoteObjectReference;

/**
 *
 */
public class RemoteServiceManagerImpl implements RemoteServiceManager, Serializable
{
	protected transient Logger	logger	;
	private transient RPC		rpc;

	public RemoteServiceManagerImpl(RPC rpc)
	{
		init(rpc);
	}
	
	
	public void init(RPC rpc)
	{
		this.rpc = rpc;
		logger	= LoggerFactory.getLogger(getClass());
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
		getStatisticsService();
		return new FetchServiceImpl(group, user, new BandwidthStatisticsServiceImpl());
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
	public FetchService getFetchService(User userInfo)
	{
		User user = ServerUtils.getUser(userInfo.getEmail());
		if(null == user || !user.getPasswd().equals(userInfo.getPasswd()))
		{
			logger.error("Invalid username/passwd.");
			throw new AuthRuntimeException("Invalid username/passwd.");
		}
		Group group = ServerUtils.getGroup(user.getGroup());
		List<RemoteObject> ros = ServerUtils.loadRemoteObjects();
		for(RemoteObject ro : ros)
		{
			RemoteObjectReference rf = ro.getRemoteRef();
			if(null == rf)
			{
				continue;
			}
			if(rf.getImpl() instanceof FetchService)
			{
				FetchServiceImpl impl = (FetchServiceImpl)rf.getImpl();
				if(impl.user.getEmail().equals(userInfo.getEmail()))
				{
					impl.setUserAndGroup(group, user);
					return (FetchService)rpc.exportRemoteObject(impl, rf.getObjID());
				}
			}
		}
		FetchServiceImpl impl = new FetchServiceImpl(group, user, new BandwidthStatisticsServiceImpl());
		return (FetchService)rpc.exportRemoteObject(impl);
	}

	@Override
	public AccountService getAccountService(User userInfo)
	{
		User user = ServerUtils.getUser(userInfo.getEmail());
		if(null == user || !user.getPasswd().equals(userInfo.getPasswd()))
		{
			logger.error("Invalid username/passwd.");
			throw new AuthRuntimeException("Invalid username/passwd.");
		}
		try
		{
			List<RemoteObject> ros = ServerUtils.loadRemoteObjects();
			for(RemoteObject ro : ros)
			{
				RemoteObjectReference rf = ro.getRemoteRef();
				if(null == rf)
				{
					continue;
				}
				if(rf.getImpl() instanceof AccountService)
				{
					AccountServiceImpl impl = (AccountServiceImpl)rf.getImpl();
					if(impl.user.getEmail().equals(userInfo.getEmail()))
					{
						return (AccountService)rpc.exportRemoteObject(impl, rf.getObjID());
					}
				}
			}
			Group group = ServerUtils.getGroup(user.getGroup());
			AccountServiceImpl impl = new AccountServiceImpl(group, user);

			return (AccountService)rpc.exportRemoteObject(impl);
		}
		catch(Throwable e)
		{
			logger.error("Failed to get account service", e);
			return null;
		}
	}

	@Override
	public BandwidthStatisticsService getBandwidthStatisticsService(User user)
	{
		User root = ServerUtils.getUser(Constants.ROOT_NAME);
		if(null == user || !user.getEmail().equals(Constants.ROOT_NAME) || !user.getPasswd().equals(root.getPasswd()))
		{
			logger.error("Invalid username/passwd.");
			throw new AuthRuntimeException("Invalid username/passwd, MUST be root user");
		}
		return getStatisticsService();
	}

	private BandwidthStatisticsService getStatisticsService()
	{
		List<RemoteObject> ros = ServerUtils.loadRemoteObjects();
		for(RemoteObject ro : ros)
		{
			RemoteObjectReference rf = ro.getRemoteRef();
			if(null == rf)
			{
				continue;
			}
			if(rf.getImpl() instanceof BandwidthStatisticsService)
			{
				return (BandwidthStatisticsService)rpc.exportRemoteObject(rf.getImpl(), rf.getObjID());
			}
		}
		BandwidthStatisticsService impl = new BandwidthStatisticsServiceImpl();
		return (BandwidthStatisticsService)rpc.exportRemoteObject(impl);
	}
}
