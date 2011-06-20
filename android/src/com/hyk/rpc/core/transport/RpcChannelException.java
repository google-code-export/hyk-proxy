/**
 * 
 */
package com.hyk.rpc.core.transport;

/**
 * @author Administrator
 *
 */
public class RpcChannelException extends Exception {

	public RpcChannelException(String message)
	{
		super(message);
	}
	public RpcChannelException(String message, Throwable e)
	{
		super(message, e);
	}
}
