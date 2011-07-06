/**
 * This file is part of the hyk-proxy-android project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ConfigService.java 
 *
 * @author yinqiwen [ 2011-7-5 | ÏÂÎç09:03:26 ]
 *
 */
package org.hyk.proxy.android.config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import org.hyk.proxy.android.R;
import org.hyk.proxy.framework.util.SimpleSocketAddress;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hyk.proxy.client.util.GoogleAvailableService;
import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.Version;
import com.hyk.proxy.common.secure.NoneSecurityService;

/**
 *
 */
public class Config
{

	protected static Logger logger = LoggerFactory.getLogger(Config.class);
	private Context ctx;
	private SimpleSocketAddress localProxyServerAddress = new SimpleSocketAddress();

	private int threadPoolSize = 30;
	private static Config instance = null;

	private Config(Context ctx)
	{
		this.ctx = ctx;
	}

	public static void initSingletonInstance(Context ctx)
	{
		if (null == instance)
		{
			instance = new Config(ctx);

		}
		else
		{
			instance.ctx = ctx;
		}
		try
		{
			instance.init();
		}
		catch (Exception e)
		{
			logger.error("Failed to load config", e);
		}
	}

	public static Config loadConfig()
	{
		return instance;
	}

	public static Config getInstance()
	{
		return instance;
	}

	private String proxyEventServiceFactory = "GAE";

	public String getProxyEventServiceFactory()
	{
		return proxyEventServiceFactory;
	}

	public void init() throws Exception
	{
		SharedPreferences pref = PreferenceManager
		        .getDefaultSharedPreferences(ctx);
		// General part
		localProxyServerAddress.host = pref.getString(
		        ctx.getString(R.string.PROXY_SERVICE_HOST), "localhost");
		try
		{
			localProxyServerAddress.port = Integer.parseInt(pref.getString(ctx
			        .getString(R.string.PROXY_SERVICE_PORT).trim(), "48100"));
		}
		catch (Exception e)
		{
			localProxyServerAddress.port = 48100;
		}

		try
		{
			threadPoolSize = Integer.parseInt(pref.getString(
			        ctx.getString(R.string.THREAD_POOL_SIZE).trim(), "20"));
		}
		catch (Exception e)
		{
			threadPoolSize = 20;
		}

		// Google AppEngine part
		client2ServerConnectionMode = ConnectionMode.fromString(pref.getString(
		        ctx.getString(R.string.GAE_CONNECTION_MODE), "HTTP"));
		String appids = pref.getString(
		        ctx.getString(R.string.GOOGLE_APPID_LIST), "");
		if (!appids.equals(""))
		{
			String[] appid_list = appids.split(",");
			for (int i = 0; i < appid_list.length; i++)
			{
				String appid = appid_list[i];
				HykProxyServerAuth auth = HykProxyServerAuth.fromStr(appid
				        .trim());
				hykProxyServerAuths.add(auth);
			}
		}
		
		String xmpp_accounts_str = pref.getString(
		        ctx.getString(R.string.XMPP_ACCOUNTS), "");
		if (!xmpp_accounts_str.equals(""))
		{
			String[] xmpp_account_list = xmpp_accounts_str.split(",");
			for (int i = 0; i < xmpp_account_list.length; i++)
			{
				String xmpp_account = xmpp_account_list[i].trim();
				XmppAccount account = XmppAccount.fromStr(xmpp_account);
				xmppAccounts.add(account);
			}
		}
		
		String localProxyStr = pref.getString(
		        ctx.getString(R.string.LOCAL_PROXY), "");
		localProxyStr = localProxyStr.trim();
		if(localProxyStr.length() != 0)
		{
			ProxyInfo info = ProxyInfo.fromStr(localProxyStr);
			localProxy = info;
		}
			
		postInit();
	}

	public SimpleSocketAddress getLocalProxyServerAddress()
	{
		return localProxyServerAddress;
	}

	public int getThreadPoolSize()
	{
		return threadPoolSize;
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

		public static ConnectionMode fromString(String s)
		{
			if (s.equalsIgnoreCase("HTTPS"))
			{
				return HTTPS2GAE;
			}
			else if (s.equalsIgnoreCase("HTTP"))
			{
				return HTTP2GAE;
			}
			else if (s.equalsIgnoreCase("XMPP"))
			{
				return XMPP2GAE;
			}
			return HTTP2GAE;
		}

	}

	public static class HykProxyServerAuth
	{

		public String appid;

		public String user;

		public String passwd;

		public static HykProxyServerAuth fromStr(String str)
		{
			HykProxyServerAuth auth = new HykProxyServerAuth();
			if (str.indexOf("@") == -1)
			{
				auth.appid = str.trim();
			}
			else
			{
				String[] temp = str.split("@");
				auth.appid = temp[1].trim();
				auth.user = temp[0].split(":")[0].trim();
				auth.passwd = temp[0].split(":")[1].trim();
			}
			return auth;
		}
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

		public static ProxyInfo fromStr(String str)
		{
			String proxystr = str;
			ProxyInfo info = new ProxyInfo();
			if (str.startsWith("https://"))
			{
				info.type = ProxyType.HTTPS;
				proxystr = proxystr.substring("https://".length());
			}
			else if (str.startsWith("http://"))
			{
				info.type = ProxyType.HTTP;
				proxystr = proxystr.substring("http://".length());
			}
			String server_part = proxystr;
			String user_part = null;
			if (proxystr.indexOf("@") != -1)
			{
				server_part = proxystr.split("@")[1].trim();
				user_part = proxystr.split("@")[0].trim();
			}

			if (server_part.indexOf(":") != -1)
			{
				info.host = server_part.split(":")[0].trim();
				info.port = Integer.parseInt(server_part.split(":")[1].trim());
			}
			else
			{
				info.host = server_part;
				if (info.type.equals(ProxyType.HTTPS))
				{
					info.port = 443;
				}
				else
				{
					info.port = 80;
				}
			}
			
			if(null != user_part)
			{
				if (user_part.indexOf(":") != -1)
				{
					info.user = user_part.split(":")[0].trim();
					info.passwd = user_part.split(":")[1].trim();
				}
				else
				{
					info.user = user_part;
					info.passwd = "";
				}
			}
			return info;
		}

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

		public static XmppAccount fromStr(String str)
		{
			XmppAccount account = new XmppAccount();
			if (str.indexOf("@") == -1)
			{
				return null;
			}
			else
			{
				String[] temp = str.split("@");
				String uaser_part = temp[0].trim();
				String server_part = temp[1].trim();
				// account.name = uaser_part.split(":")[0].trim();
				account.passwd = uaser_part.split(":")[1].trim();
				if (server_part.indexOf(":") != -1)
				{
					account.serverPort = Integer.parseInt(server_part
					        .split(":")[1]);
				}
				account.jid = uaser_part.split(":")[0].trim() + "@"
				        + server_part.split(":")[0].trim();
			}
			return account.init();
		}

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

	private List<XmppAccount> xmppAccounts = new LinkedList<Config.XmppAccount>();

	public void setXmppAccounts(List<XmppAccount> xmppAccounts)
	{
		this.xmppAccounts = xmppAccounts;
	}

	private int httpConnectionPoolSize = 7;

	public void setHttpConnectionPoolSize(int httpConnectionPoolSize)
	{
		this.httpConnectionPoolSize = httpConnectionPoolSize;
	}

	private List<String> injectRangeHeaderSiteSet = new ArrayList<String>();
	private String injectRangeHeaderSites = "youtube.com";

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

	private int rpcTimeOut = 45;

	public void setRpcTimeOut(int rpcTimeOut)
	{
		this.rpcTimeOut = rpcTimeOut;
	}

	private boolean simpleURLEnable = true;

	public void setSimpleURLEnable(boolean simpleURLEnable)
	{
		this.simpleURLEnable = simpleURLEnable;
	}

	private String compressor = "lzf";

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

	private ConnectionMode client2ServerConnectionMode = ConnectionMode.HTTP2GAE;

	public ConnectionMode getClient2ServerConnectionMode()
	{
		return client2ServerConnectionMode;
	}

	public void setClient2ServerConnectionMode(
	        ConnectionMode client2ServerConnectionMode)
	{
		this.client2ServerConnectionMode = client2ServerConnectionMode;
	}

	private String httpUpStreamEncrypter = "se1";

	public String getHttpUpStreamEncrypter()
	{
		return httpUpStreamEncrypter;
	}

	public void setHttpUpStreamEncrypter(String httpUpStreamEncrypter)
	{
		this.httpUpStreamEncrypter = httpUpStreamEncrypter;
	}

	public void postInit() throws Exception
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

	public String getPreferenceValue(String key)
	{
		return null;
	}

	public void setPrefernceValue(String key, String value)
	{

	}
}
