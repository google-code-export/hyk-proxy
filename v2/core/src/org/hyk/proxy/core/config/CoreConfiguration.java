package org.hyk.proxy.core.config;

import java.util.Properties;

public class CoreConfiguration extends BasicConfiguration
{
	private String listenHost = "localhost";
	private int listenPort = 48100;
	private int threadpoolSize = 30;
	
	public String getListenHost()
	{
		return listenHost;
	}

	public int getListenPort()
	{
		return listenPort;
	}

	public int getThreadPoolSize()
	{
		return threadpoolSize;
	}

	@Override
    protected String getTagName()
    {
	    return "Core";
    }

	@Override
    protected void doInit(Properties props)
    {
		listenHost = props.getProperty("ListenHost");
		String portstr = props.getProperty("ListenPort");
		String threadpoolStr = props.getProperty("ThreadPoolSize");
		listenPort = Integer.parseInt(portstr);
		threadpoolSize = Integer.parseInt(threadpoolStr);
    }

}
