/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: FetchService.java 
 *
 * @author yinqiwen [ Jan 25, 2010 | 2:37:50 PM ]
 *
 */
package com.hyk.proxy.common.rpc.service;

import com.hyk.proxy.common.http.message.HttpRequestExchange;
import com.hyk.proxy.common.http.message.HttpResponseExchange;
import com.hyk.rpc.core.annotation.Remote;

/**
 *
 */
@Remote
public interface FetchService
{
	HttpResponseExchange fetch(HttpRequestExchange req);
}
