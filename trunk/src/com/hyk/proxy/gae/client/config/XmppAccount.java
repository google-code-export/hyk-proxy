/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: XmppAccount.java 
 *
 * @author yinqiwen [ 2010-1-31 | 10:50:02 AM]
 *
 */
package com.hyk.proxy.gae.client.config;

import javax.net.ssl.SSLSocketFactory;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;

/**
 *
 */
public class XmppAccount
{
	
	private static final String GTALK_SERVER = "talk.google.com";
	private static final String GTALK_SERVER_NAME = "gmail.com";
	private static final int GTALK_SERVER_PORT = 5222;
	
	private static final String OVI_SERVER = "chat.ovi.com";
	private static final String OVI_SERVER_NAME = "ovi.com";
	private static final int OVI_SERVER_PORT = 5223;

	
	protected static final int DEFAULT_PORT = 5222;
	
	public XmppAccount(String jid, String passwd)
	{
		super();
		this.jid = jid;
		String server = StringUtils.parseServer(jid).trim();
		if(server.equals(GTALK_SERVER_NAME))
		{
			this.serverHost = GTALK_SERVER;
			this.serverPort = GTALK_SERVER_PORT;
			this.name = jid;
		}
		else if(server.equals(OVI_SERVER_NAME))
		{
			this.serverHost = OVI_SERVER;
			this.serverPort = OVI_SERVER_PORT;
			this.name =  StringUtils.parseName(jid);
			this.isOldSSLEnable = true;
		}
		else
		{
			this.serverHost = server;
			this.serverPort = DEFAULT_PORT;
			this.name =  StringUtils.parseName(jid);
			
		}
		this.serviceName = server;
		this.passwd = passwd;
		
	}
	
	public String getJid() 
	{
		return jid;
	}
	
	public String getServerHost() 
	{
		return serverHost;
	}

	public void setServerHost(String serverHost) 
	{
		this.serverHost = serverHost;
	}

	public int getServerPort() 
	{
		return serverPort;
	}

	public void setServerPort(int serverPort) 
	{
		this.serverPort = serverPort;
	}

	public String getServiceName() 
	{
		return serviceName;
	}

	public void setServiceName(String serviceName) 
	{
		this.serviceName = serviceName;
	}
	
	public String getName()
	{
		return name;
	}

	public String getPasswd()
	{
		return passwd;
	}
	
	public ConnectionConfiguration getConnectionConfig() 
	{
		return connectionConfig;
	}
	
	public boolean isOldSSLEnable() 
	{
		return isOldSSLEnable;
	}

	public void setOldSSLEnable(boolean isOldSSLEnable) 
	{
		this.isOldSSLEnable = isOldSSLEnable;
	}
	
	public XmppAccount init()
	{
		connectionConfig = new ConnectionConfiguration(serverHost, serverPort,serviceName);
		if(isOldSSLEnable)
		{
			connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
			connectionConfig.setSocketFactory(SSLSocketFactory.getDefault());
		}
		return this;
	}

	protected String	name;
	protected String	passwd;
	protected String    jid;

	protected String    serverHost;
	protected int       serverPort;
	protected String    serviceName;
	protected boolean   isOldSSLEnable;


	protected ConnectionConfiguration connectionConfig;


}
