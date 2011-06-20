/**
 * 
 */
package com.hyk.rpc.core;

/**
 * @author Administrator
 *
 */
public class Rpctimeout extends RuntimeException {

	public Rpctimeout(String message)
	{
		super(message);
	}
	public Rpctimeout(String message, Throwable e)
	{
		super(message, e);
	}
}
