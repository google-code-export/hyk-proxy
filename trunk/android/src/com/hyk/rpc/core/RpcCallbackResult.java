/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RpcCallbackResult.java 
 *
 * @author qiying.wang [ Mar 29, 2010 | 10:57:16 AM ]
 *
 */
package com.hyk.rpc.core;

/**
 *
 */
public class RpcCallbackResult<ParameterType> 
{
	private ParameterType result;
	private Throwable exception;
	
	public RpcCallbackResult(ParameterType result, Throwable exception)
	{
		super();
		this.result = result;
		this.exception = exception;
	}
	
	public ParameterType get() throws Throwable
	{
		if(null != exception)
		{
			throw exception;
		}
		return result;
	}

}
