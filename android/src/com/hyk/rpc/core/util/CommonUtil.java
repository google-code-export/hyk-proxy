/**
 * 
 */
package com.hyk.rpc.core.util;

import com.hyk.rpc.core.session.SessionManager;

/**
 * @author Administrator
 * 
 */
public class CommonUtil {

	private static ThreadLocal<SessionManager> rpcInstanceTable = new ThreadLocal<SessionManager>();

	public static SessionManager getSessionManager() {
		return rpcInstanceTable.get();
	}

	public static void setSessionManager(SessionManager rpc) {
		rpcInstanceTable.set(rpc);
	}

}
