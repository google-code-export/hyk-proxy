/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Config.java 
 *
 * @author yinqiwen [ 2010-5-14 | 08:49:33 PM]
 *
 */
package com.hyk.proxy.client.config;

import java.io.IOException;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;

import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.secure.NoneSecurityService;

/**
 *
 */
@XmlRootElement(name = "Configure")
public class Config
{
	public static enum ConnectionMode
	{
		HTTP2GAE(1), XMPP2GAE(2);
		int	value;

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
		@XmlAttribute
		public String	host;
		@XmlAttribute
		public int		port;
	}

	public static class HykProxyServerAuth
	{
		@XmlAttribute
		public String	appid;
		@XmlAttribute
		public String	user;
		@XmlAttribute
		public String	passwd;
	}

	public static class ProxyInfo
	{
		@XmlAttribute
		public String	host;
		@XmlAttribute
		public int		port	= 80;
		@XmlAttribute
		public String	user;
		@XmlAttribute
		public String	passwd;
	}

	public static class XmppAccount
	{
		private static final String	GTALK_SERVER		= "talk.google.com";
		private static final String	GTALK_SERVER_NAME	= "gmail.com";
		private static final int	GTALK_SERVER_PORT	= 5222;

		private static final String	OVI_SERVER			= "chat.ovi.com";
		private static final String	OVI_SERVER_NAME		= "ovi.com";
		private static final int	OVI_SERVER_PORT		= 5223;

		protected static final int	DEFAULT_PORT		= 5222;

		public XmppAccount init()
		{
			String server = StringUtils.parseServer(jid).trim();
			// String name = null;
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
				this.name = StringUtils.parseName(jid);
				this.isOldSSLEnable = true;
			}
			else
			{
				if(null == this.serverHost || this.serverHost.isEmpty())
				{
					this.serverHost = server;
				}
				this.name = StringUtils.parseName(jid);
			}
			String serviceName = server;
			connectionConfig = new ConnectionConfiguration(this.serverHost, serverPort, serviceName);
			if(isOldSSLEnable)
			{
				connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
				connectionConfig.setSocketFactory(SSLSocketFactory.getDefault());
			}

			return this;
		}

		@XmlAttribute(name = "user")
		public String					jid;
		@XmlAttribute
		public String					passwd;
		@XmlAttribute
		public int						serverPort	= DEFAULT_PORT;
		@XmlAttribute
		public String					serverHost;
		@XmlAttribute(name = "oldSSLEnable")
		public boolean					isOldSSLEnable;

		@XmlTransient
		public ConnectionConfiguration	connectionConfig;
		@XmlTransient
		public String					name;

	}

	@XmlElement(name = "localserver")
	private SimpleSocketAddress			localProxyServerAddress;

	private List<HykProxyServerAuth>	hykProxyServerAuths;

	@XmlElements(@XmlElement(name = "hyk-proxy-server"))
	public void setHykProxyServerAuths(List<HykProxyServerAuth> hykProxyServerAuths)
	{
		this.hykProxyServerAuths = hykProxyServerAuths;
	}

	private List<XmppAccount>	xmppAccounts;

	@XmlElements(@XmlElement(name = "XMPPAccount"))
	public void setXmppAccounts(List<XmppAccount> xmppAccounts)
	{
		this.xmppAccounts = xmppAccounts;
	}

	private int	threadPoolSize;

	@XmlElement
	public void setThreadPoolSize(int threadPoolSize)
	{
		this.threadPoolSize = threadPoolSize;
	}

	private int	httpConnectionPoolSize;

	@XmlElement
	public void setHttpConnectionPoolSize(int httpConnectionPoolSize)
	{
		this.httpConnectionPoolSize = httpConnectionPoolSize;
	}

	private int	rpcTimeOut;

	@XmlElement(name = "RPCTimeOut")
	public void setRpcTimeOut(int rpcTimeOut)
	{
		this.rpcTimeOut = rpcTimeOut;
	}

	private boolean	simpleURLEnable;

	@XmlElement
	public void setSimpleURLEnable(boolean simpleURLEnable)
	{
		this.simpleURLEnable = simpleURLEnable;
	}

	private String	compressor;

	@XmlElement
	public void setCompressor(String compressor)
	{
		this.compressor = compressor;
	}

	private int	fetchLimitSize;

	@XmlElement
	public void setFetchLimitSize(int fetchLimitSize)
	{
		this.fetchLimitSize = fetchLimitSize;
	}

	private int	maxFetcherNumber;

	@XmlElement
	public void setMaxFetcherNumber(int maxFetcherNumber)
	{
		this.maxFetcherNumber = maxFetcherNumber;
	}

	private ProxyInfo	localProxy;

	@XmlElement(name = "localProxy")
	public void setHykProxyClientLocalProxy(ProxyInfo localProxy)
	{
		this.localProxy = localProxy;
	}

	@XmlElement
	private ProxyInfo		defaultLocalProxy;

	private ConnectionMode	client2ServerConnectionMode;

	@XmlTransient
	public ConnectionMode getClient2ServerConnectionMode()
	{
		return client2ServerConnectionMode;
	}

	public void setClient2ServerConnectionMode(ConnectionMode client2ServerConnectionMode)
	{
		this.client2ServerConnectionMode = client2ServerConnectionMode;
	}

	private String	httpUpStreamEncrypter;

	public String getHttpUpStreamEncrypter()
	{
		return httpUpStreamEncrypter;
	}

	@XmlElement
	public void setHttpUpStreamEncrypter(String httpUpStreamEncrypter)
	{
		this.httpUpStreamEncrypter = httpUpStreamEncrypter;
	}

	public void init()
	{
		for(HykProxyServerAuth auth : hykProxyServerAuths)
		{
			if(auth.user == null || auth.user.equals(""))
			{
				auth.user = Constants.ANONYMOUSE_NAME;
			}
			if(auth.passwd == null || auth.passwd.equals(""))
			{
				auth.passwd = Constants.ANONYMOUSE_NAME;
			}
		}
		if(client2ServerConnectionMode.equals(ConnectionMode.XMPP2GAE) && null != xmppAccounts)
		{
			for(XmppAccount account : xmppAccounts)
			{
				account.init();
			}
		}
		if(localProxy != null && null == localProxy.host)
		{
			localProxy = null;
		}

		if(null == httpUpStreamEncrypter)
		{
			httpUpStreamEncrypter = NoneSecurityService.NAME;
		}
	}

	@XmlElement
	void setConnectionMode(int mode)
	{
		client2ServerConnectionMode = ConnectionMode.fromInt(mode);
	}

	int getConnectionMode()
	{
		return client2ServerConnectionMode.value;
	}

	public SimpleSocketAddress getLocalProxyServerAddress()
	{
		return localProxyServerAddress;
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

	public int getThreadPoolSize()
	{
		return threadPoolSize;
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

	public ProxyInfo getHykProxyClientLocalProxy()
	{
		return localProxy;
	}

	public void activateDefaultProxy()
	{
		if(null == localProxy)
		{
			localProxy = defaultLocalProxy;
		}
	}

	public static Config getInstance() throws IOException
	{
		try
		{
			return ConfigService.getDefaultConfig();
		}
		catch(Exception e)
		{
			throw new IOException(e);
		}
	}
}
