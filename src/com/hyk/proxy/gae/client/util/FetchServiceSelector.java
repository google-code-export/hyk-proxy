/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: XmppAccount.java 
 *
 * @author yinqiwen [ 2010-1-31 | 10:50:02 AM]
 *
 */
package com.hyk.proxy.gae.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hyk.proxy.gae.common.service.AsyncFetchService;
import com.hyk.proxy.gae.common.service.FetchService;
import com.hyk.rpc.core.RpcException;
import com.hyk.rpc.core.util.RpcUtil;

/**
 * @author yinqiwen
 *
 */
public class FetchServiceSelector 
{
	private List<FetchService>		fetchServices;
	private List<AsyncFetchService> asyncFetchServices;
	private int						cursor;
	
	public FetchServiceSelector(List<FetchService> fetchServices) throws RpcException 
	{
		Collections.shuffle(fetchServices);
		this.fetchServices = fetchServices;
		asyncFetchServices = new ArrayList<AsyncFetchService>();
		for(FetchService service:fetchServices)
		{
			asyncFetchServices.add(RpcUtil.asyncWrapper(service, AsyncFetchService.class));
		}
	}

	public synchronized FetchService select()
	{
		if(cursor >= fetchServices.size())
		{
			cursor = 0;
		}
		return fetchServices.get(cursor++);
	}
	
	public synchronized AsyncFetchService selectAsync()
	{
		if(cursor >= asyncFetchServices.size())
		{
			cursor = 0;
		}
		return asyncFetchServices.get(cursor++);
	}

}
