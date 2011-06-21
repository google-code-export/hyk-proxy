/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ManageResource.java 
 *
 * @author qiying.wang [ May 17, 2010 | 11:35:25 AM ]
 *
 */
package org.hyk.proxy.framework.management;

/**
 *
 */
public interface ManageResource
{
	public String handleManagementCommand(String cmd);
	public String getName();
}
