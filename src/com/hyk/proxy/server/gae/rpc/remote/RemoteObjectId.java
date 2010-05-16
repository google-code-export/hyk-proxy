/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RemoteObjectId.java 
 *
 * @author yinqiwen [ 2010-4-18 | обнГ08:32:47 ]
 *
 */
package com.hyk.proxy.server.gae.rpc.remote;

import javax.persistence.Id;

/**
 *
 */
public class RemoteObjectId
{
	@Id
    private Long id;
	
	private String temp = "hyk-proxy-remote-object";
	
	public long getId()
	{
		return id;
	}
}
