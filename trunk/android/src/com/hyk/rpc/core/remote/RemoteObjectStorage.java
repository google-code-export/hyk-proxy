/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteObjectStorage.java 
 *
 * @author qiying.wang [ May 11, 2010 | 4:48:52 PM ]
 *
 */
package com.hyk.rpc.core.remote;

import java.util.List;

/**
 *
 */
public interface RemoteObjectStorage
{
	public void store(RemoteObjectReference remoteObject);
	public List<RemoteObjectReference> loadAll();
	
}
