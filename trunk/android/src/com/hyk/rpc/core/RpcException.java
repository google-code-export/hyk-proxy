/**
 * 
 */
package com.hyk.rpc.core;

/**
 * @author Administrator
 *
 */
public class RpcException extends Exception {

	public RpcException(String message)
	{
		super(message);
	}
	public RpcException(String message, Throwable e)
	{
		super(message, e);
	}
}
