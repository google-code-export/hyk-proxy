/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: XmppAccount.java 
 *
 * @author yinqiwen [ 2010-1-31 | 10:50:02 AM]
 *
 */
package com.hyk.proxy.gae.client.netty;

import java.util.Collections;
import java.util.List;

import com.hyk.proxy.gae.common.service.FetchService;

/**
 * @author yinqiwen
 *
 */
public class FetchServiceSelector 
{
	private List<FetchService>		fetchServices;
	private int						cursor;
	
	public FetchServiceSelector(List<FetchService> fetchServices) 
	{
		super();
		Collections.shuffle(fetchServices);
		this.fetchServices = fetchServices;
	}

	public synchronized FetchService select()
	{
		if(cursor >= fetchServices.size())
		{
			cursor = 0;
		}
		return fetchServices.get(cursor++);
	}

}
