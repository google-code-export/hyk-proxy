/**
 * 
 */
package org.arch.event.http;

/**
 * @author qiyingwang
 *
 */
public interface HTTPEventContants
{
	public static final int HTTP_REQUEST_EVENT_TYPE = 1000;
	public static final int HTTP_RESPONSE_EVENT_TYPE = 1001;
	public static final int HTTP_CHUNK_EVENT_TYPE = 1002;
	public static final int HTTP_ERROR_EVENT_TYPE = 1003;
	public static final int HTTP_CONNECTION_EVENT_TYPE = 1004;
}
