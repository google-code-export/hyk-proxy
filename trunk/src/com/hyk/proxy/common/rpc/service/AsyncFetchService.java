/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AsyncFetchService.java 
 *
 * @author yinqiwen [ 2010-3-28 | 08:55:32 PM ]
 *
 */
package com.hyk.proxy.common.rpc.service;


import com.hyk.proxy.common.http.message.HttpRequestExchange;
import com.hyk.proxy.common.http.message.HttpResponseExchange;
import com.hyk.rpc.core.RpcCallback;
import com.hyk.rpc.core.annotation.Async;

/**
 *
 */
@Async(FetchService.class)
public interface AsyncFetchService
{
	void fetch(HttpRequestExchange req, RpcCallback<HttpResponseExchange> callback);
}
