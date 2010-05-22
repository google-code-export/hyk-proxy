/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SecurityServiceFactory.java 
 *
 * @author yinqiwen [ 2010-5-15 | 10:39:00 PM ]
 *
 */
package com.hyk.proxy.common.secure;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class SecurityServiceFactory
{
	
	public static class RegistSecurityService
	{
		public RegistSecurityService(SecurityService service, int id)
		{
			this.service = service;
			this.id = id;
		}

		public final SecurityService	service;
		public final int		id;
	}
	private static Map<String, RegistSecurityService>	NAME_TO_REGISTE_SECURITY_SERVICE_TABLE	= new ConcurrentHashMap<String, RegistSecurityService>();
	private static Map<Integer, RegistSecurityService>	ID_TO_REGISTE_SECURITY_SERVICE_TABLE		= new ConcurrentHashMap<Integer, RegistSecurityService>();

	private static AtomicInteger					ID_SEED								= new AtomicInteger(0);
	
	static
	{
		registerSecurityService(new NoneSecurityService());
	}
	
	public static Collection<RegistSecurityService> getAllRegistSecurityServices()
	{
		return NAME_TO_REGISTE_SECURITY_SERVICE_TABLE.values();
	}
	
	public static RegistSecurityService getRegistSecurityService(String name)
	{
		return NAME_TO_REGISTE_SECURITY_SERVICE_TABLE.get(name);
	}
	
	public static RegistSecurityService getRegistSecurityService(int id)
	{
		return ID_TO_REGISTE_SECURITY_SERVICE_TABLE.get(id);
	}
	
	public static RegistSecurityService registerSecurityService(SecurityService service)
	{
		if(NAME_TO_REGISTE_SECURITY_SERVICE_TABLE.containsKey(service.getName()))
		{
			return NAME_TO_REGISTE_SECURITY_SERVICE_TABLE.get(service.getName());
		}
		int id = ID_SEED.getAndIncrement();
		RegistSecurityService reg = new RegistSecurityService(service, id);
		NAME_TO_REGISTE_SECURITY_SERVICE_TABLE.put(service.getName(), reg);
		ID_TO_REGISTE_SECURITY_SERVICE_TABLE.put(id, reg);
		return reg;
	}
}
