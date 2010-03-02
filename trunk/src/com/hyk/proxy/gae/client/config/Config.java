/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Config.java 
 *
 * @author yinqiwen [ 2010-1-31 | 10:41:52 AM ]
 *
 */
package com.hyk.proxy.gae.client.config;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


import com.hyk.compress.CompressorType;

/**
 *
 */
public class Config
{
	private static final String	CONFIG_FILE						= "hyk-proxy-client.properties";
	private static final String	APPID_CONFIG					= "remoteserver.appid";
	private static final String	XMPP_USER_CONFIG				= "localserver.xmpp.user";
	private static final String	XMPP_PASS_CONFIG				= "localserver.xmpp.passwd";
	private static final String	XMPP_SERVER_HOST_CONFIG			= "localserver.xmpp.server.host";
	private static final String	XMPP_SERVER_PORT_CONFIG			= "localserver.xmpp.server.port";
	private static final String	XMPP_SERVER_OLDSSL_CONFIG		= "localserver.xmpp.oldssl.enable";
	
	private static final String	IS_HTTP_ENABLE					= "localserver.http.enable";
	private static final String	IS_XMPP_ENABLE					= "localserver.xmpp.enable";
	private static final String	LOCAL_SERVER_HOST				= "localserver.host";
	private static final String	LOCAL_SERVER_PORT				= "localserver.port";
	private static final String	LOCAL_SERVER_SESSION_TIMEOUT	= "localserver.rpc.timeout";
	private static final String	LOCAL_SERVER_HTTP_FETCH_LIMIT	= "localserver.rpc.http.fetchlimitsize";
	private static final String	LOCAL_SERVER_COMPRESSOR_TYPE			= "localserver.rpc.compressor.type";
	private static final String	LOCAL_SERVER_COMPRESSOR_TRIGGER			= "localserver.rpc.compressor.trigger";
	private static final String   LOCAL_SERVER_HTTP_MAX_FETCHER           = "localserver.rpc.http.maxfetcher";
	private static final String   LOCAL_SERVER_HTTP_CONNECTION_POOL_SIZE  = "localserver.http.connection_pool_size";
	
	public static final String	STOP_COMMAND					= "StopLocalServer";

	private List<String>		appids							= new LinkedList<String>();
	private List<XmppAccount>	accounts						= new LinkedList<XmppAccount>();

	private CompressorType		compressorType					= CompressorType.GZ;
	private int compressorTrigger = 256;
	private int httpConnectionPoolSize = 5;
	

	private static Config		instance						= null;

	public synchronized static Config getInstance() throws IOException
	{
		if(null == instance)
		{
			instance = new Config();
		}
		return instance;
	}

	private Config() throws IOException
	{
		loadConfig();
	}

	public int getHttpConnectionPoolSize()
	{
		return httpConnectionPoolSize;
	}
	
	public String getLocalServerHost()
	{
		return localServerHost;
	}

	public int getLocalServerPort()
	{
		return localServerPort;
	}

	private String	localServerHost	= "127.0.0.1";
	private int		localServerPort	= 48100;
	private int		sessionTimeout	= 30 * 1000;

	private int		fetchLimitSize	= 327680;
	private int     maxFetcherForBigFile = 5;

	private boolean	isXmppEnable;
	private boolean	isHttpEnable;

	public int getMaxFetcherForBigFile() 
	{
		return maxFetcherForBigFile;
	}

	
	public List<String> getAppids()
	{
		return appids;
	}

	public List<XmppAccount> getAccounts()
	{
		return accounts;
	}

	public int getSessionTimeout()
	{
		return sessionTimeout;
	}

	public int getFetchLimitSize()
	{
		return fetchLimitSize;
	}

	public boolean isXmppEnable()
	{
		return isXmppEnable;
	}

	public boolean isHttpEnable()
	{
		return isHttpEnable;
	}
	
	public CompressorType getCompressorType()
	{
		return compressorType;
	}

	public int getCompressorTrigger()
	{
		return compressorTrigger;
	}

	private void loadConfig() throws IOException
	{
		Properties props = new Properties();
		props.load(Config.class.getResourceAsStream("/" + CONFIG_FILE));

		Iterator keys = props.keySet().iterator();
		while(keys.hasNext())
		{
			String key = (String)keys.next();
			String value = props.getProperty(key);
			if(null == value || value.trim().isEmpty())
			{
				continue;
			}
			value = value.trim();
			if(key.startsWith(APPID_CONFIG))
			{
				appids.add(value);
			}
			else if(key.startsWith(XMPP_USER_CONFIG))
			{
				String name = value;
				String id = key.substring(XMPP_USER_CONFIG.length());
				String passwdkey = XMPP_PASS_CONFIG + id;
				String passwd = props.getProperty(passwdkey);
				if(null == passwd)
				{
					throw new IOException("Configuration:" + passwdkey + " is missing.");
				}
				XmppAccount account = new XmppAccount(name, passwd);
				String host = props.getProperty(XMPP_SERVER_HOST_CONFIG + id);
				if(null != host && !host.trim().isEmpty())
				{
					account.setServerHost(host);
				}
				String port = props.getProperty(XMPP_SERVER_PORT_CONFIG + id);
				if(null != port && !port.trim().isEmpty())
				{
					account.setServerPort(Integer.parseInt(port.trim()));
				}
				String oldssl = props.getProperty(XMPP_SERVER_OLDSSL_CONFIG + id);
				if(null != oldssl && !oldssl.trim().isEmpty())
				{
					account.setOldSSLEnable(Boolean.parseBoolean(oldssl.trim()));
				}
				accounts.add(account.init());
			}
			else if(key.equals(IS_HTTP_ENABLE))
			{
				isHttpEnable = Boolean.parseBoolean(value);
			}
			else if(key.equals(IS_XMPP_ENABLE))
			{
				isXmppEnable = Boolean.parseBoolean(value);
			}
			else if(key.equals(LOCAL_SERVER_HOST))
			{
				localServerHost = value;
			}
			else if(key.equals(LOCAL_SERVER_PORT))
			{
				localServerPort = Integer.parseInt(value);
			}
			else if(key.equals(LOCAL_SERVER_SESSION_TIMEOUT))
			{
				sessionTimeout = Integer.parseInt(value) * 1000;
			}
			else if(key.equals(LOCAL_SERVER_HTTP_FETCH_LIMIT))
			{
				fetchLimitSize = Integer.parseInt(value);
			}
			else if(key.equals(LOCAL_SERVER_COMPRESSOR_TYPE))
			{
				compressorType = CompressorType.valueOfName(value);
			}
			else if(key.equals(LOCAL_SERVER_COMPRESSOR_TRIGGER))
			{
				compressorTrigger = Integer.parseInt(value);
			}
			else if(key.equals(LOCAL_SERVER_HTTP_MAX_FETCHER))
			{
				maxFetcherForBigFile = Integer.parseInt(value);
			}
			else if(key.equals(LOCAL_SERVER_HTTP_CONNECTION_POOL_SIZE))
			{
				httpConnectionPoolSize = Integer.parseInt(value);
			}
		}

	}
}
