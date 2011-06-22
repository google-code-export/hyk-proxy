/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Config.java 
 *
 * @author yinqiwen [ 2010-5-14 | 08:49:33 PM]
 *
 */
package org.hyk.proxy.gae.client.config;

import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import org.hyk.proxy.gae.client.util.GoogleAvailableService;
import org.hyk.proxy.gae.common.Constants;
import org.hyk.proxy.gae.common.Version;
import org.hyk.proxy.gae.common.secure.NoneSecurityService;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */

public class Config
{
	protected static Logger logger = LoggerFactory.getLogger(Config.class);
	private static Config instance = null;

	static
	{
		try
		{

			instance.init();
		}
		catch (Exception e)
		{
			logger.error("Failed to load default config file!", e);
		}
	}

	public static enum ConnectionMode
	{
		HTTP2GAE(1), XMPP2GAE(2), HTTPS2GAE(3);
		int value;

		ConnectionMode(int v)
		{
			value = v;
		}

		public static ConnectionMode fromInt(int v)
		{
			return values()[v - 1];
		}

	}

	public static class SimpleSocketAddress
	{

		public String host;

		public int port;
	}

	public static class HykProxyServerAuth
	{

		public String appid;

		public String user;

		public String passwd;
	}

	public static enum ProxyType
	{
		HTTP("http"), HTTPS("https");
		String value;

		ProxyType(String v)
		{
			value = v;
		}

		public static ProxyType fromStr(String str)
		{
			if (str.equalsIgnoreCase("http"))
			{
				return HTTP;
			}
			if (str.equalsIgnoreCase("https"))
			{
				return HTTPS;
			}
			return HTTP;
		}
	}

	public static class ProxyInfo
	{

		public String host;

		public int port = 80;

		public String user;

		public String passwd;

		public ProxyType type = ProxyType.HTTP;

		public String nextHopGoogleServer;

	}

	public static class XmppAccount
	{
		private static final String GTALK_SERVER = "talk.google.com";
		private static final String GTALK_SERVER_NAME = "gmail.com";
		private static final int GTALK_SERVER_PORT = 5222;

		private static final String OVI_SERVER = "chat.ovi.com";
		private static final String OVI_SERVER_NAME = "ovi.com";
		private static final int OVI_SERVER_PORT = 5223;

		protected static final int DEFAULT_PORT = 5222;

		public XmppAccount init()
		{
			String server = StringUtils.parseServer(jid).trim();
			// String name = null;
			if (server.equals(GTALK_SERVER_NAME))
			{
				if (null == this.serverHost || this.serverHost.equals(""))
				{
					this.serverHost = GTALK_SERVER;
				}
				if (0 == this.serverPort)
				{
					this.serverPort = GTALK_SERVER_PORT;
				}

				this.name = jid;
			}
			else if (server.equals(OVI_SERVER_NAME))
			{
				if (null == this.serverHost || this.serverHost.equals(""))
				{
					this.serverHost = OVI_SERVER;
				}
				if (0 == this.serverPort)
				{
					this.serverPort = OVI_SERVER_PORT;
				}
				this.name = StringUtils.parseName(jid);
				this.isOldSSLEnable = true;
			}
			else
			{
				if (null == this.serverHost || this.serverHost.equals(""))
				{
					this.serverHost = server;
				}
				if (0 == this.serverPort)
				{
					this.serverPort = DEFAULT_PORT;
				}
				this.name = StringUtils.parseName(jid);
			}
			String serviceName = server;
			connectionConfig = new ConnectionConfiguration(this.serverHost,
			        serverPort, serviceName);
			if (isOldSSLEnable)
			{
				connectionConfig
				        .setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
				connectionConfig
				        .setSocketFactory(SSLSocketFactory.getDefault());
			}

			return this;
		}

		public String jid;

		public String passwd;

		public int serverPort;

		public String serverHost;

		public boolean isOldSSLEnable;

		public ConnectionConfiguration connectionConfig;

		public String name;

	}

	private List<HykProxyServerAuth> hykProxyServerAuths = new LinkedList<HykProxyServerAuth>();

	public void setHykProxyServerAuths(
	        List<HykProxyServerAuth> hykProxyServerAuths)
	{
		this.hykProxyServerAuths = hykProxyServerAuths;
	}

	private List<XmppAccount> xmppAccounts;

	public void setXmppAccounts(List<XmppAccount> xmppAccounts)
	{
		this.xmppAccounts = xmppAccounts;
	}

	private int httpConnectionPoolSize;

	public void setHttpConnectionPoolSize(int httpConnectionPoolSize)
	{
		this.httpConnectionPoolSize = httpConnectionPoolSize;
	}

	private List<String> injectRangeHeaderSiteSet = new ArrayList<String>();
	private String injectRangeHeaderSites;

	void setInjectRangeHeaderSites(String injectRangeHeaderSites)
	{
		this.injectRangeHeaderSites = injectRangeHeaderSites;
		String[] sites = injectRangeHeaderSites.split(";");
		for (String s : sites)
		{
			injectRangeHeaderSiteSet.add(s.trim());
		}
		// System.out.println("#####" + injectRangeHeaderSiteSet);
		// System.exit(1);

	}

	String getInjectRangeHeaderSites()
	{
		return injectRangeHeaderSites;
	}

	public boolean isInjectRangeHeaderSitesMatchHost(String host)
	{
		for (String site : injectRangeHeaderSiteSet)
		{
			if (!site.equals("") && host.indexOf(site) != -1)
			{
				return true;
			}
		}
		return false;
	}

	private int rpcTimeOut;

	public void setRpcTimeOut(int rpcTimeOut)
	{
		this.rpcTimeOut = rpcTimeOut;
	}

	private boolean simpleURLEnable;

	public void setSimpleURLEnable(boolean simpleURLEnable)
	{
		this.simpleURLEnable = simpleURLEnable;
	}

	private String compressor;

	public void setCompressor(String compressor)
	{
		this.compressor = compressor;
	}

	private int fetchLimitSize;

	public void setFetchLimitSize(int fetchLimitSize)
	{
		this.fetchLimitSize = fetchLimitSize;
	}

	private int maxFetcherNumber;

	public void setMaxFetcherNumber(int maxFetcherNumber)
	{
		this.maxFetcherNumber = maxFetcherNumber;
	}

	// @XmlElement
	private ProxyInfo localProxy;

	public void setHykProxyClientLocalProxy(ProxyInfo localProxy)
	{
		this.localProxy = localProxy;
	}

	// @XmlElement(name = "localProxy")
	public ProxyInfo getHykProxyClientLocalProxy()
	{
		return localProxy;
	}

	// @XmlElement
	// private ProxyInfo defaultLocalProxy;

	private ConnectionMode client2ServerConnectionMode;

	public ConnectionMode getClient2ServerConnectionMode()
	{
		return client2ServerConnectionMode;
	}

	public void setClient2ServerConnectionMode(
	        ConnectionMode client2ServerConnectionMode)
	{
		this.client2ServerConnectionMode = client2ServerConnectionMode;
	}

	private String httpUpStreamEncrypter;

	public String getHttpUpStreamEncrypter()
	{
		return httpUpStreamEncrypter;
	}

	public void setHttpUpStreamEncrypter(String httpUpStreamEncrypter)
	{
		this.httpUpStreamEncrypter = httpUpStreamEncrypter;
	}

	public void init() throws Exception
	{
		if (localProxy != null)
		{
			if (null != localProxy.host)
			{
				localProxy.host = localProxy.host.trim();
			}
			if (null != localProxy.nextHopGoogleServer)
			{
				localProxy.nextHopGoogleServer = localProxy.nextHopGoogleServer
				        .trim();
				if (localProxy.nextHopGoogleServer.equals(""))
				{
					localProxy.nextHopGoogleServer = null;
				}
			}
			if (null == localProxy.host || localProxy.host.equals(""))
			{
				localProxy = null;
			}
		}

		// if (defaultLocalProxy != null
		// && (null == defaultLocalProxy.host || defaultLocalProxy.host
		// .isEmpty()))
		// {
		// defaultLocalProxy = null;
		// }
		if (null != hykProxyServerAuths)
		{
			for (int i = 0; i < hykProxyServerAuths.size(); i++)
			{
				HykProxyServerAuth auth = hykProxyServerAuths.get(i);
				if (auth.appid == null || auth.appid.trim().equals(""))
				{
					hykProxyServerAuths.remove(i);
					i--;
					continue;
				}
				if (auth.user == null || auth.user.equals(""))
				{
					auth.user = Constants.ANONYMOUSE_NAME;
				}
				if (auth.passwd == null || auth.passwd.equals(""))
				{
					auth.passwd = Constants.ANONYMOUSE_NAME;
				}
				auth.appid = auth.appid.trim();
				auth.user = auth.user.trim();
				auth.passwd = auth.passwd.trim();
				// if(null == localProxy &&
				// !ClientUtils.isHTTPServerReachable(auth.appid))
				// {
				// activateDefaultProxy();
				// }
			}
		}

		if (client2ServerConnectionMode.equals(ConnectionMode.XMPP2GAE))
		{
			for (int i = 0; i < xmppAccounts.size(); i++)
			{
				XmppAccount account = xmppAccounts.get(i);
				if (account.jid == null || account.jid.equals(""))
				{
					xmppAccounts.remove(i);
					i--;
				}
				else
				{
					account.init();
				}
			}
		}
		if (client2ServerConnectionMode.equals(ConnectionMode.XMPP2GAE)
		        && (null == xmppAccounts || xmppAccounts.isEmpty()))
		{
			throw new Exception("Since the connection mode is "
			        + ConnectionMode.XMPP2GAE
			        + ", at least one XMPP account needed.");
		}

		if (null == httpUpStreamEncrypter)
		{
			httpUpStreamEncrypter = NoneSecurityService.NAME;
		}

		if (localProxy == null || localProxy.host.contains("google"))
		{
			simpleURLEnable = true;
		}
	}


	void setConnectionMode(int mode)
	{
		client2ServerConnectionMode = ConnectionMode.fromInt(mode);
	}

	int getConnectionMode()
	{
		return client2ServerConnectionMode.value;
	}

	public List<HykProxyServerAuth> getHykProxyServerAuths()
	{
		return hykProxyServerAuths;
	}

	public int getHttpConnectionPoolSize()
	{
		return httpConnectionPoolSize;
	}

	public int getFetchLimitSize()
	{
		return fetchLimitSize;
	}

	public int getRpcTimeOut()
	{
		return rpcTimeOut;
	}

	public boolean isSimpleURLEnable()
	{
		return simpleURLEnable;
	}

	public String getCompressor()
	{
		return compressor;
	}

	public int getMaxFetcherNumber()
	{
		return maxFetcherNumber;
	}

	public List<XmppAccount> getXmppAccounts()
	{
		return xmppAccounts;
	}

	public void clearProxy()
	{
		localProxy = null;
	}

	public boolean selectDefaultHttpProxy()
	{
		if (null == localProxy)
		{
			ProxyInfo info = new ProxyInfo();
			info.host = GoogleAvailableService.getInstance()
			        .getAvailableHttpService();
			if (null != info.host)
			{
				localProxy = info;
				return true;
			}
		}
		return false;
	}

	public boolean selectDefaultHttpsProxy()
	{
		if (null == localProxy)
		{
			ProxyInfo info = new ProxyInfo();
			info.host = GoogleAvailableService.getInstance()
			        .getAvailableHttpsService();
			info.port = 443;
			info.type = ProxyType.HTTPS;
			if (null != info.host)
			{
				localProxy = info;
				return true;
			}
		}
		return false;
	}

	private List<AppIdBinding> appIdBindings;

	private HttpProxyUserAgent httpProxyUserAgent;

	static class AppIdBinding
	{

		String appid;

		List<String> sites;
	}

	static class HttpProxyUserAgent
	{

		String choice;

		List<UserAgent> agents;
	}

	static class UserAgent
	{

		String name;

		String value;
	}

	public String getBindingAppId(String host)
	{
		if (null != appIdBindings)
		{
			for (AppIdBinding binding : appIdBindings)
			{
				for (String site : binding.sites)
				{
					if (host.contains(site))
					{
						return binding.appid.trim();
					}
				}
			}
		}
		return null;
	}

	public String getSimulateUserAgent()
	{
		String defaultUserAgent = Constants.PROJECT_NAME + " V" + Version.value;
		if (null != httpProxyUserAgent)
		{
			String choice = httpProxyUserAgent.choice;
			List<UserAgent> list = httpProxyUserAgent.agents;
			for (UserAgent ua : list)
			{
				if (ua.name.equals(choice))
				{
					return ua.value.trim();
				}
			}
		}
		return defaultUserAgent;
	}

	public static Config getInstance()
	{
		return instance;
	}

	public void saveConfig() throws Exception
	{
		try
		{
			init();
			//TODO
		}
		catch (Exception e)
		{
			throw e;
		}
	}
}
