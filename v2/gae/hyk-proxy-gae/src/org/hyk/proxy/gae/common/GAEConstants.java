/**
 * 
 */
package org.hyk.proxy.gae.common;

/**
 * @author qiyingwang
 *
 */
public interface GAEConstants
{
	public static final String CLIENT_CONF_NAME = "gae.xml";
	public static final String ANONYMOUSE_NAME = "anonymous";
	public static final String HTTP_INVOKE_PATH = "/eventbus";
	
	public static final int APPENGINE_HTTP_BODY_LIMIT = 2000000; //2MB
	public static final int APPENGINE_XMPP_BODY_LIMIT = 4096; //4KB
	
	public static final int FETCH_FAILED = -2;
	public static final int RESPONSE_TOO_LARGE = -3;
	public static final int RESPONSE_TIMEOUT = -4;
	
	public static final int AUTH_REQUEST_EVENT_TYPE = 2000;
	public static final int AUTH_RESPONSE_EVENT_TYPE = 2001;
}
