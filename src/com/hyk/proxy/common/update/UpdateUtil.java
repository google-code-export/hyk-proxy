/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: UpdateUtil.java 
 *
 * @author yinqiwen [ 2010-5-16 | обнГ06:27:50 ]
 *
 */
package com.hyk.proxy.common.update;

import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.http.message.HttpRequestExchange;

/**
 *
 */
public class UpdateUtil
{
	public static HttpRequestExchange createVersionQueryHttpRequest()
	{
		HttpRequestExchange req = new HttpRequestExchange();
		req.method = "GET";
		req.url = Constants.LATEST_VERSION;
		return req;
	}
}
