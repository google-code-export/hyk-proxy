/**
 * 
 */
package org.hyk.proxy.gae.client.handler;

/**
 * @author qiyingwang
 * 
 */
public enum ProxySessionStatus
{
	INITED,
	WAITING_NORMAL_RESPONSE, 
	WAITING_MULTI_RANGE_RESPONSE, 
	SESSION_COMPLETED, 
	WATING_RANGE_UPLOAD_RESPONSE
}
