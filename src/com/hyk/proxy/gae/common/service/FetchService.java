/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: FetchService.java 
 *
 * @author qiying.wang [ Jan 25, 2010 | 2:37:50 PM ]
 *
 */
package com.hyk.proxy.gae.common.service;

import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.rpc.core.remote.Remote;

/**
 *
 */
@Remote
public interface FetchService
{
	HttpResponseExchange fetch(HttpRequestExchange req);
}
