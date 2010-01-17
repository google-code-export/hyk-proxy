/**
 * 
 */
package com.hyk.proxy.gae.client;

/**
 * @author Administrator
 *
 */
public class TalkException extends Exception {

	public int getResCode() {
		return resCode;
	}

	public String getResCause() {
		return resCause;
	}

	private int resCode;
	private String resCause;
	
	public TalkException(int resCode,String cause)
	{
		super(cause);
		this.resCause = cause;
		this.resCode = resCode;
	}

}
