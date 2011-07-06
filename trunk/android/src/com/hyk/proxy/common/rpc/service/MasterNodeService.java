/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AppIdShareService.java 
 *
 * @author qiying.wang [ May 17, 2010 | 5:19:27 PM ]
 *
 */
package com.hyk.proxy.common.rpc.service;

import java.util.List;

import com.hyk.rpc.core.annotation.Remote;

/**
 *
 */
@Remote
public interface MasterNodeService
{
	public static final String NAME = "MASTER_NODE_SERVICE";
	public String shareMyAppId(String appid, String gmail);
	public String unshareMyAppid(String appid, String gmail);
	public List<String> randomRetrieveAppIds();
	public String getVersion();
	
}
