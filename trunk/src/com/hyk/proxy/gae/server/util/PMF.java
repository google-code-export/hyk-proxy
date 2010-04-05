/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: PMF.java 
 *
 * @author yinqiwen [ 2010-3-28 | 09:46:57 PM]
 *
 */

package com.hyk.proxy.gae.server.util;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

public final class PMF
{
	private static final PersistenceManagerFactory	pmfInstance	= JDOHelper.getPersistenceManagerFactory("transactions-optional");

	private PMF()
	{
	}

	public static PersistenceManagerFactory get()
	{
		return pmfInstance;
	}
}
