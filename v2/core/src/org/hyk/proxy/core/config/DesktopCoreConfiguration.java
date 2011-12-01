/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Config.java 
 *
 * @author yinqiwen [ 2010-5-14 | 08:49:33 PM]
 *
 */
package org.hyk.proxy.core.config;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hyk.proxy.core.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@XmlRootElement(name = "Configure")
public class DesktopCoreConfiguration implements CoreConfiguration
{
	protected static Logger logger = LoggerFactory.getLogger(DesktopCoreConfiguration.class);

	private static DesktopCoreConfiguration instance = null;

	static
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(DesktopCoreConfiguration.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			instance = (DesktopCoreConfiguration) unmarshaller.unmarshal(DesktopCoreConfiguration.class
			        .getResource("/" + Constants.CONF_FILE));			
			instance.init();
		}
		catch (Exception e)
		{
			logger.error("Failed to load default config file!", e);
		}
	}

	@XmlElement(name = "LocalServer")
	private SimpleSocketAddress localProxyServerAddress = new SimpleSocketAddress(
	        "localhost", 48100);

	private int threadPoolSize = 30;

	@XmlElement(name = "ThreadPoolSize")
	public void setThreadPoolSize(int threadPoolSize)
	{
		this.threadPoolSize = threadPoolSize;
	}

	private String proxyEventServiceHandler = "GAE";

	public String getProxyEventHandler()
	{
		return proxyEventServiceHandler;
	}

	@XmlElement
	public void setProxyEventHandler(String handlerName)
	{
		this.proxyEventServiceHandler = handlerName;
	}

	public void init() throws Exception
	{

	}

	public SimpleSocketAddress getLocalProxyServerAddress()
	{
		return localProxyServerAddress;
	}

	public int getThreadPoolSize()
	{
		return threadPoolSize;
	}

	private DesktopCoreConfiguration()
	{
		// nothing
	}

	public static DesktopCoreConfiguration getInstance()
	{
		return instance;
	}
}
