/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: MixApplicationConfig.java 
 *
 * @author yinqiwen [ 2010-5-22 | 11:38:31 AM ]
 *
 */
package com.hyk.proxy.client.application.mix;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;


import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.application.gae.GoogleAppEngineApplicationConfig;
import com.hyk.proxy.client.application.seattle.event.SeattleProxyEventServiceFactory;
import com.hyk.proxy.client.framework.event.HttpProxyEvent;
import com.hyk.proxy.client.framework.event.HttpProxyEventType;


/**
 *
 */
@XmlRootElement(name="Configure")
public class MixApplicationConfig
{
	static Logger		logger	= LoggerFactory.getLogger(MixApplicationConfig.class);
	
	private static MixApplicationConfig instance;
//	static Properties	config	= new Properties();
//	static String[] delegateURLPattern;
//	static HttpProxyEventType[] delegateEvent;
//	static String[] delegateMethods;
	
	@XmlElement(name="DelegateEventServiceFactoryClass")
	private String delegateEventServiceFactoryClass;
	
	@XmlElement(name="DelegateRules")
	private DelegateRules rules;
	
	static class DelegateRules
	{
		@XmlElements(@XmlElement(name = "MatchHost"))
		private List<String> matchHosts;
		
		@XmlElements(@XmlElement(name = "MatchUrl"))
		private List<String> matchUrls;
		
		@XmlElements(@XmlElement(name = "Method"))
		private List<String> matchMethods;
		
		@XmlElements(@XmlElement(name = "MatchEventType"))
		private List<String> matchEventTypes;
	}
	
	static
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(MixApplicationConfig.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			instance = (MixApplicationConfig)unmarshaller.unmarshal(MixApplicationConfig.class.getResource("/app-mix.xml"));
		}
		catch(Exception e)
		{
			logger.error("Failed to load application-GAE's config.", e);
		}
	}
	
	public static boolean matchDelegateRules(HttpProxyEvent event)
	{
		if(null != instance.rules)
		{
			if(null != instance.rules.matchEventTypes)
			{
				for(String type:instance.rules.matchEventTypes)
				{
					if(event.getType().equals(Enum.valueOf(HttpProxyEventType.class, type.trim())))
					{
						return true;
					}
				}
			}
			
			if(event.getSource() instanceof HttpRequest)
			{
				HttpRequest req = (HttpRequest)event.getSource();
				if(null != instance.rules.matchHosts)
				{
					String host = req.getHeader(HttpHeaders.Names.HOST);
					if(null != host)
					{
						for(String matchHost:instance.rules.matchHosts)
						{
							if(matchHost.trim().isEmpty())
							{
								continue;
							}
							if(host.contains(matchHost.trim()))
							{
								return true;
							}
						}
					}
				}
				
				if(null != instance.rules.matchMethods)
				{
					String method = req.getMethod().getName();
					for(String matchMethod:instance.rules.matchMethods)
					{
						if(matchMethod.trim().equalsIgnoreCase(method))
						{
							return true;
						}
					}
				}
				
				if(null != instance.rules.matchUrls)
				{
					String url = req.getUri();
					for(String matchUrl:instance.rules.matchUrls)
					{
						if(matchUrl.trim().isEmpty())
						{
							continue;
						}
						if(url.contains(matchUrl.trim()))
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static String getDelegateEventServiceFactoryClass()
	{
		return instance.delegateEventServiceFactoryClass;
	}
}
