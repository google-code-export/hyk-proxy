/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RequestListener.java 
 *
 * @author qiying.wang [ Mar 2, 2010 | 2:29:48 PM ]
 *
 */
package com.hyk.rpc.core;

import com.hyk.rpc.core.message.Request;
import com.hyk.rpc.core.session.Session;

/**
 *
 */
public interface RequestListener
{
	public void processRequest(Session session, Request req) throws Exception;
}
