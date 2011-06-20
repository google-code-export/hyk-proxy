/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RpcCallBack.java 
 *
 * @author qiying.wang [ Mar 1, 2010 | 3:01:05 PM ]
 *
 */
package com.hyk.rpc.core;

public interface RpcCallback<ParameterType>
{
	void callBack(RpcCallbackResult<ParameterType> result);
}
