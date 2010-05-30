/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: GoogleAppEngineApplicationConfig.java 
 *
 * @author yinqiwen [ 2010-5-22 | 10:53:00 AM ]
 *
 */
package com.hyk.proxy.client.application.gae;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.common.Version;

/**
 *
 */
@XmlRootElement(name="Configure")
public class GoogleAppEngineApplicationConfig
{
	static Logger		logger	= LoggerFactory.getLogger(GoogleAppEngineApplicationConfig.class);
	//static Properties	config	= new Properties();
	static GoogleAppEngineApplicationConfig instance;
	
	@XmlElementWrapper(name="AppIdBindings")
	@XmlElements(@XmlElement(name = "Binding"))
	private List<AppIdBinding> appIdBindings;
	
	@XmlElement(name="HttpProxyUserAgent")
	private HttpProxyUserAgent httpProxyUserAgent;
	
	static class AppIdBinding
	{
		@XmlAttribute
		String appid;
		@XmlElements(@XmlElement(name = "site"))
		List<String> sites;
	}
	
	static class HttpProxyUserAgent
	{
		@XmlAttribute
		String choice;
		@XmlElements(@XmlElement(name = "UserAgent"))
		List<UserAgent> agents;
	}
	
	static class UserAgent
	{
		@XmlAttribute
		String name;
		@XmlValue
		String value;
	}
	
	static
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(GoogleAppEngineApplicationConfig.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			instance = (GoogleAppEngineApplicationConfig)unmarshaller.unmarshal(GoogleAppEngineApplicationConfig.class.getResource("/app-gae.xml"));
			instance.init();
		}
		catch(Exception e)
		{
			logger.error("Failed to load application-GAE's config.", e);
		}
	}
	
	private void init()
	{
		if(null != httpProxyUserAgent)
		{
			List<UserAgent> list = httpProxyUserAgent.agents;
			for(UserAgent ua:list)
			{
				ua.value = ua.value.trim();
			}
		}
	}
	
	public static String getBindingAppId(String host)
	{
		if(null != instance.appIdBindings)
		{
			for(AppIdBinding binding:instance.appIdBindings)
			{
				for(String site:binding.sites)
				{
					if(host.contains(site))
					{
						return binding.appid;
					}
				}
			}
		}	
		return null;
	}
	
	public static String getSimulateUserAgent()
	{
		String defaultUserAgent = "hyk-proxy-client V" + Version.value;  
		if(null != instance.httpProxyUserAgent)
		{
			String choice = instance.httpProxyUserAgent.choice;
			List<UserAgent> list = instance.httpProxyUserAgent.agents;
			for(UserAgent ua:list)
			{
				if(ua.name.equals(choice))
				{
					return ua.value;
				}
			}
		}
		return defaultUserAgent;
	}
	
	public static void main(String [] args) throws Exception
	{
		JAXBContext context = JAXBContext.newInstance(GoogleAppEngineApplicationConfig.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		GoogleAppEngineApplicationConfig config = (GoogleAppEngineApplicationConfig)unmarshaller.unmarshal(GoogleAppEngineApplicationConfig.class.getResource("/app-gae.xml"));
		System.out.println(config.appIdBindings.size());
		System.out.println(config.appIdBindings.get(0).appid);
		System.out.println(config.appIdBindings.get(0).sites.size());
		System.out.println(config.appIdBindings.get(0).sites.get(1));
		System.out.println(config.httpProxyUserAgent.choice);
		System.out.println(config.httpProxyUserAgent.agents.size());
		System.out.println(config.httpProxyUserAgent.agents.get(0).name);
		System.out.println(config.httpProxyUserAgent.agents.get(0).value);
		String host="xyz@appspot.com";
		System.out.println( host.substring(0, host.indexOf('@')));
	}
}
