/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ResponseListener.java 
 *
 * @author qiying.wang [ Mar 2, 2010 | 2:30:34 PM ]
 *
 */
package com.hyk.rpc.core;

import com.hyk.rpc.core.message.Response;
import com.hyk.rpc.core.session.Session;

/**
 *
 */
public interface ResponseListener
{
	public void processResponse(Session session, Response res) throws Exception;
	
}
