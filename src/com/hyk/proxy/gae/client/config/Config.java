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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.hyk.compress.compressor.gz.GZipCompressor;
import com.hyk.proxy.gae.client.util.ClientUtils;

/**
 *
 */
public class Config
{
	public static final String	CONFIG_FILE						= "hyk-proxy-client.conf";
	public static final String	APPID_CONFIG					= "remoteserver.appid";
	public static final String	XMPP_USER_CONFIG				= "localserver.xmpp.user";
	public static final String	XMPP_PASS_CONFIG				= "localserver.xmpp.passwd";
	public static final String	XMPP_SERVER_HOST_CONFIG			= "localserver.xmpp.server.host";
	public static final String	XMPP_SERVER_PORT_CONFIG			= "localserver.xmpp.server.port";
	public static final String	XMPP_SERVER_OLDSSL_CONFIG		= "localserver.xmpp.oldssl.enable";
	
	public static final String	IS_HTTP_ENABLE					= "localserver.http.enable";
	public static final String	IS_XMPP_ENABLE					= "localserver.xmpp.enable";
	public static final String	LOCAL_SERVER_HOST				= "localserver.host";
	public static final String	LOCAL_SERVER_PORT				= "localserver.port";
	public static final String	LOCAL_SERVER_SESSION_TIMEOUT	= "localserver.rpc.timeout";
	public static final String	LOCAL_SERVER_HTTP_FETCH_LIMIT	= "localserver.rpc.http.fetchlimitsize";
	public static final String	LOCAL_SERVER_COMPRESSOR_TYPE			= "localserver.rpc.compressor.type";
	public static final String	LOCAL_SERVER_COMPRESSOR_TRIGGER			= "localserver.rpc.compressor.trigger";
	public static final String   LOCAL_SERVER_HTTP_MAX_FETCHER           = "localserver.rpc.http.maxfetcher";
	public static final String   LOCAL_SERVER_HTTP_CONNECTION_POOL_SIZE  = "localserver.http.connection_pool_size";
	
	public static final String   LOCAL_SERVER_HTTP_PROXY_HOST  = "localserver.http.proxy.host";
	public static final String   LOCAL_SERVER_HTTP_PROXY_PORT  = "localserver.http.proxy.port";
	public static final String   LOCAL_SERVER_HTTP_PROXY_USER  = "localserver.http.proxy.user";
	public static final String   LOCAL_SERVER_HTTP_PROXY_PASSWD  = "localserver.http.proxy.password";
	
	public static final String   LOCAL_SERVER_THREAD_POOL_SIZE  = "localserver.threadpoolsize";
	
	public static final String   LOCAL_SERVER_HTTP_PROXY_HOST_DEFAULT  = "localserver.http.proxy.host.default";
	public static final String   LOCAL_SERVER_HTTP_PROXY_PORT_DEFAULT  = "localserver.http.proxy.port.default";
	
	public static final String	STOP_COMMAND					= "StopLocalServer";

	private List<AppIdAuth>		appids							= new LinkedList<AppIdAuth>();
	private List<XmppAccount>	accounts						= new LinkedList<XmppAccount>();

	private String		compressorName					= GZipCompressor.NAME;
	private int compressorTrigger = 256;
	private int httpConnectionPoolSize = 5;
	private int threadPoolSize = 20;
	
	private ProxyInfo proxy;
	
	private ProxyInfo defaultProxy = new ProxyInfo("www.google.com.hk");
	
	private Properties wholeProps;

	private static Config		instance						= null;

	public synchronized static Config getInstance() throws IOException
	{
		if(null == instance)
		{
			Properties props = new Properties();
			props.load(Config.class.getResourceAsStream("/" + CONFIG_FILE));
			return getInstance(props);
		}
		return instance;
	}
	
	public synchronized static Config getInstance(Properties props) throws IOException
	{
		instance = new Config(props);
		return instance;
	}

	private Config(Properties props) throws IOException
	{
		loadFromProperties(props);
		init();
		this.wholeProps = props;
	}
	
	private void init()
	{
		for(AppIdAuth appid:appids)
		{
			if(null == proxy && !ClientUtils.isHTTPServerReachable(appid.getAppid()))
			{
				activateDefaultProxy();
				break;
			}
		}
		if(!isHttpEnable && isXmppEnable)
		{
			if(accounts.isEmpty())
			{
				throw new IllegalArgumentException("No XMPP account found when XMPP mode enabled!");
			}
			for(XmppAccount account:accounts)
			{
				account.init();
			}
		}
		
		for(AppIdAuth appid:appids)
		{
			if(appid.getEmail() == null || appid.getEmail().equals(""))
			{
				appid.setEmail("anonymouse");
			}
			if(appid.getPasswd() == null || appid.getPasswd().equals(""))
			{
				appid.setPasswd("anonymouse");
			}
		}
		
	}

	public ProxyInfo getProxyInfo()
	{
		return proxy;
	}
	
	public void activateDefaultProxy()
	{
		proxy = defaultProxy;
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

	private String	localServerHost	= "localhost";
	private int		localServerPort	= 48100;
	private int		sessionTimeout	= 30 * 1000;

	private int		fetchLimitSize	= 327680;
	private int     maxFetcherForBigFile = 5;

	private boolean	isXmppEnable;
	private boolean	isHttpEnable;

	public int getThreadPoolSize()
	{
		return threadPoolSize;
	}
	
	public int getMaxFetcherForBigFile() 
	{
		return maxFetcherForBigFile;
	}
	
	public List<AppIdAuth> getAppids()
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
	
	public String getCompressorName()
	{
		return compressorName;
	}

	public int getCompressorTrigger()
	{
		return compressorTrigger;
	}

	private Config loadFromProperties(Properties props) throws IOException
	{
		Iterator keys = props.keySet().iterator();
		while(keys.hasNext())
		{
			String key = (String)keys.next();
			String value = props.getProperty(key);
			if(null == value || value.trim().equals(""))
			{
				continue;
			}
			value = value.trim();
			if(key.startsWith(APPID_CONFIG) && (!key.contains("auth.email") && !key.contains("auth.passwd")))
			{
				AppIdAuth auth = new AppIdAuth();
				auth.setAppid(value);
				String email = props.getProperty(key + ".auth.email");
				String passwd = props.getProperty(key + ".auth.passwd");
				auth.setEmail(email);
				auth.setPasswd(passwd);
				appids.add(auth);
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
				if(null != host && !host.trim().equals(""))
				{
					account.setServerHost(host);
				}
				String port = props.getProperty(XMPP_SERVER_PORT_CONFIG + id);
				if(null != port && !port.trim().equals(""))
				{
					account.setServerPort(Integer.parseInt(port.trim()));
				}
				String oldssl = props.getProperty(XMPP_SERVER_OLDSSL_CONFIG + id);
				if(null != oldssl && !oldssl.trim().equals(""))
				{
					account.setOldSSLEnable(Boolean.parseBoolean(oldssl.trim()));
				}
				accounts.add(account);
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
				compressorName = value.toLowerCase();
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
			else if(key.equals(LOCAL_SERVER_THREAD_POOL_SIZE))
			{
				threadPoolSize = Integer.parseInt(value);
			}
			else if(key.equals(LOCAL_SERVER_HTTP_PROXY_HOST))
			{
				proxy = new ProxyInfo();
				proxy.setHost(value);
				String port = props.getProperty(LOCAL_SERVER_HTTP_PROXY_PORT);
				if(null != port && !port.trim().equals(""))
				{
					proxy.setPort(Integer.parseInt(port.trim()));
				}
				String user = props.getProperty(LOCAL_SERVER_HTTP_PROXY_USER);
				if(null != user && !user.trim().equals(""))
				{
					proxy.setUser(user.trim());
				}
				String passwd = props.getProperty(LOCAL_SERVER_HTTP_PROXY_PASSWD);
				if(null != passwd && !passwd.trim().equals(""))
				{
					proxy.setPassword(passwd.trim());
				}
			}
			else if(key.equals(LOCAL_SERVER_HTTP_PROXY_HOST_DEFAULT))
			{
				defaultProxy = new ProxyInfo();
				defaultProxy.setHost(value);
				String port = props.getProperty(LOCAL_SERVER_HTTP_PROXY_PORT_DEFAULT);
				if(null != port && !port.trim().equals(""))
				{
					defaultProxy.setPort(Integer.parseInt(port.trim()));
				}
			}
		}
		
		return this;
	}
	
	public void storeToConf(String comment) throws IOException
	{
		URL url = Config.class.getResource("/" + CONFIG_FILE);
		String conf = url.getFile();
		FileOutputStream fos = new FileOutputStream(conf);
		wholeProps.store(fos, comment);
		fos.close();
	}
}


