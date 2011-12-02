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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hyk.proxy.gae.common.CompressorType;
import org.hyk.proxy.gae.common.EncryptType;
import org.hyk.proxy.gae.common.GAEConstants;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@XmlRootElement(name = "Configure")
public class GAEClientConfiguration
{
	protected static Logger logger = LoggerFactory
	        .getLogger(GAEClientConfiguration.class);
	private static GAEClientConfiguration instance = null;

	static
	{
		try
		{
			JAXBContext context = JAXBContext
			        .newInstance(GAEClientConfiguration.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			instance = (GAEClientConfiguration) unmarshaller
			        .unmarshal(GAEClientConfiguration.class.getResource("/"
			                + GAEConstants.CLIENT_CONF_NAME));
			instance.init();
		}
		catch (Exception e)
		{
			logger.error("Failed to load default config file!", e);
		}
	}

	public static enum ConnectionMode
	{
		HTTP, HTTPS, XMPP;
	}

	public static class GAEServerAuth
	{
		@XmlAttribute
		public String appid;
		@XmlAttribute
		public String user;
		@XmlAttribute
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
		@XmlAttribute
		public String host;
		@XmlAttribute
		public int port = 80;
		@XmlAttribute
		public String user;
		@XmlAttribute
		public String passwd;

		@XmlAttribute
		public ProxyType type = ProxyType.HTTP;

		@XmlAttribute
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
				if (null == this.serverHost || this.serverHost.isEmpty())
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
				if (null == this.serverHost || this.serverHost.isEmpty())
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
				if (null == this.serverHost || this.serverHost.isEmpty())
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

		@XmlAttribute(name = "user")
		public String jid;
		@XmlAttribute
		public String passwd;
		@XmlAttribute
		public int serverPort;
		@XmlAttribute
		public String serverHost;
		@XmlAttribute(name = "oldSSLEnable")
		public boolean isOldSSLEnable;

		@XmlTransient
		public ConnectionConfiguration connectionConfig;
		@XmlTransient
		public String name;

	}

	private List<GAEServerAuth> serverAuths = new LinkedList<GAEServerAuth>();

	@XmlElementWrapper(name = "GAE")
	@XmlElements(@XmlElement(name = "Server"))
	public void setGAEServerAuths(List<GAEServerAuth> serverAuths)
	{
		this.serverAuths = serverAuths;
	}

	private List<XmppAccount> xmppAccounts;

	@XmlElementWrapper(name = "XMPP")
	@XmlElements(@XmlElement(name = "Account"))
	public void setXmppAccounts(List<XmppAccount> xmppAccounts)
	{
		this.xmppAccounts = xmppAccounts;
	}

	public List<XmppAccount> getXmppAccounts()
	{
		return xmppAccounts;
	}

	private ConnectionMode connectionMode;

	@XmlElement(name = "ConnectionMode")
	void setConnectionMode(String mode)
	{
		connectionMode = ConnectionMode.valueOf(mode);
	}

	public ConnectionMode getConnectionMode()
	{
		return connectionMode;
	}

	@XmlTransient
	public void setConnectionMode(ConnectionMode mode)
	{
		this.connectionMode = mode;
	}

	private int sessionTimeout;

	@XmlElement(name = "SessionTimeOut")
	public void setSessionTimeOut(int sessionTimeout)
	{
		this.sessionTimeout = sessionTimeout;
	}

	public int getSessionTimeOut()
	{
		return sessionTimeout;
	}

	private CompressorType compressor;

	@XmlElement(name = "Compressor")
	public void setCompressor(String compressor)
	{
		this.compressor = CompressorType.valueOf(compressor.toUpperCase());
	}

	public CompressorType getCompressor()
	{
		return compressor;
	}

	private EncryptType encrypter;

	public EncryptType getEncrypter()
	{
		return encrypter;
	}

	@XmlElement
	public void setEncrypter(String httpUpStreamEncrypter)
	{
		this.encrypter = EncryptType.valueOf(httpUpStreamEncrypter
		        .toUpperCase());
	}

	private boolean simpleURLEnable;

	@XmlElement(name = "SimpleURLEnable")
	public void setSimpleURLEnable(boolean simpleURLEnable)
	{
		this.simpleURLEnable = simpleURLEnable;
	}

	public boolean isSimpleURLEnable()
	{
		return simpleURLEnable;
	}

	private int connectionPoolSize;

	@XmlElement(name="ConnectionPoolSize")
	public void setConnectionPoolSize(int connectionPoolSize)
	{
		this.connectionPoolSize = connectionPoolSize;
	}
	public int getConnectionPoolSize()
	{
		return connectionPoolSize;
	}

	private List<String> injectRangeHeaderSiteSet = new ArrayList<String>();
	private List<String> injectRangeHeaderURLSet = new ArrayList<String>();
	private String injectRangeHeaderSites;
	private String injectRangeHeaderURLs;

	@XmlElementWrapper(name = "GAE")
	@XmlElement(name = "Sites")
	void setInjectRangeHeaderSites(String injectRangeHeaderSites)
	{
		this.injectRangeHeaderSites = injectRangeHeaderSites;
		String[] sites = injectRangeHeaderSites.split(";");
		for (String s : sites)
		{
			injectRangeHeaderSiteSet.add(s.trim());
		}
	}
	
	@XmlElementWrapper(name = "GAE")
	@XmlElement(name = "URLs")
	void setInjectRangeHeaderURLs(String injectRangeHeaderURLs)
	{
		this.injectRangeHeaderURLs = injectRangeHeaderURLs;
		String[] urls = injectRangeHeaderURLs.split(";");
		for (String s : urls)
		{
			injectRangeHeaderURLSet.add(s.trim());
		}
	}

	String getInjectRangeHeaderSites()
	{
		return injectRangeHeaderSites;
	}
	
	String getInjectRangeHeaderURLs()
	{
		return injectRangeHeaderURLs;
	}

	public boolean isInjectRangeHeaderSitesMatchHost(String host)
	{
		for (String site : injectRangeHeaderSiteSet)
		{
			if (!site.isEmpty() && host.indexOf(site) != -1)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isInjectRangeHeaderURLsMatchURL(String url)
	{
		for (String s : injectRangeHeaderURLSet)
		{
			if (!s.isEmpty() && url.indexOf(s) != -1)
			{
				return true;
			}
		}
		return false;
	}

	private int fetchLimitSize;

	@XmlElement(name = "FetchLimitSize")
	public void setFetchLimitSize(int fetchLimitSize)
	{
		this.fetchLimitSize = fetchLimitSize;
	}

	public int getFetchLimitSize()
	{
		return fetchLimitSize;
	}

	private int concurrentFetchWorker;

	@XmlElement(name = "ConcurrentRangeFetchWorker")
	public void setConcurrentRangeFetchWorker(int num)
	{
		this.concurrentFetchWorker = num;
	}

	public int getConcurrentRangeFetchWorker()
	{
		return concurrentFetchWorker;
	}

	private ProxyInfo localProxy;

	@XmlElement(name = "LocalProxy")
	public void setLocalProxy(ProxyInfo localProxy)
	{
		this.localProxy = localProxy;
	}

	public ProxyInfo getLocalProxy()
	{
		return localProxy;
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
				if (localProxy.nextHopGoogleServer.isEmpty())
				{
					localProxy.nextHopGoogleServer = null;
				}
			}
			if (null == localProxy.host || localProxy.host.isEmpty())
			{
				localProxy = null;
			}
		}
		if (null != serverAuths)
		{
			for (int i = 0; i < serverAuths.size(); i++)
			{
				GAEServerAuth auth = serverAuths.get(i);
				if (auth.appid == null || auth.appid.trim().isEmpty())
				{
					serverAuths.remove(i);
					i--;
					continue;
				}
				if (auth.user == null || auth.user.equals(""))
				{
					auth.user = GAEConstants.ANONYMOUSE_NAME;
				}
				if (auth.passwd == null || auth.passwd.equals(""))
				{
					auth.passwd = GAEConstants.ANONYMOUSE_NAME;
				}
				auth.appid = auth.appid.trim();
				auth.user = auth.user.trim();
				auth.passwd = auth.passwd.trim();
			}
		}

		if (connectionMode.equals(ConnectionMode.XMPP))
		{
			for (int i = 0; i < xmppAccounts.size(); i++)
			{
				XmppAccount account = xmppAccounts.get(i);
				if (account.jid == null || account.jid.isEmpty())
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
		if (connectionMode.equals(ConnectionMode.XMPP)
		        && (null == xmppAccounts || xmppAccounts.isEmpty()))
		{
			throw new Exception("Since the connection mode is "
			        + ConnectionMode.XMPP
			        + ", at least one XMPP account needed.");
		}

		if (null == encrypter)
		{
			encrypter = EncryptType.NONE;
		}
		if (localProxy == null || localProxy.host.contains("google"))
		{
			simpleURLEnable = true;
		}
	}

	public List<GAEServerAuth> getGAEServerAuths()
	{
		return serverAuths;
	}
	
	public GAEServerAuth getGAEServerAuth(String appid)
	{
		for(GAEServerAuth auth:serverAuths)
		{
			if(auth.appid.equals(appid))
			{
				return auth;
			}
		}
		return null;
	}

	@XmlElementWrapper(name = "AppIdBindings")
	@XmlElements(@XmlElement(name = "Binding"))
	private List<AppIdBinding> appIdBindings;
	static class AppIdBinding
	{
		@XmlAttribute
		String appid;
		@XmlElements(@XmlElement(name = "site"))
		List<String> sites;
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

	private String httpProxyUserAgent;
	public String getUserAgent()
	{
		return httpProxyUserAgent;
	}
	@XmlElement(name = "UserAgent")
	public void setUserAgent(String v)
	{
		httpProxyUserAgent = v;
	}

	public static GAEClientConfiguration getInstance()
	{
		return instance;
	}
	
	@XmlElementWrapper(name = "MappingHosts")
	@XmlElements(@XmlElement(name = "Mapping"))
	public void setMappingHosts(String src, String dst)
	{
		
	}
	
	public String getMappingHost(String src)
	{
		return src;
	}

	public void save() throws Exception
	{
		try
		{
			init();
			JAXBContext context = JAXBContext
			        .newInstance(GAEClientConfiguration.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
			URL url = GAEClientConfiguration.class.getResource("/"
			        + GAEConstants.CLIENT_CONF_NAME);
			String conf = URLDecoder.decode(url.getFile(), "UTF-8");
			FileOutputStream fos = new FileOutputStream(conf);
			// fos.write("<!-- This is generated by hyk-proxy-client GUI, it's not the orignal conf file -->\r\n".getBytes());
			marshaller.marshal(this, fos);
			fos.close();
		}
		catch (Exception e)
		{
			throw e;
		}
	}
}
