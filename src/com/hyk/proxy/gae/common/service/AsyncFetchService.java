/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AsyncFetchService.java 
 *
 * @author yinqiwen [ 2010-3-28 | обнГ08:55:32 ]
 *
 */
package com.hyk.proxy.gae.common.service;


import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
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
