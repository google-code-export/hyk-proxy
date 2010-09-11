package com.hyk.proxy.server.gae.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hyk.compress.CompressorFactory;
import com.hyk.compress.compressor.Compressor;
import com.hyk.proxy.common.Constants;

public class XmlConfig
{
	private String appId;
	private Compressor compressor;
	
	private String authErrorPage;

	private int maxXmppMessageSize;
	
	private String httpDownStreamEncrypter;
	
	private boolean isMasterNode;
	
	public boolean isMasterNode()
	{
		return isMasterNode;
	}

	public String getHttpDownStreamEncrypter()
	{
		return httpDownStreamEncrypter;
	}

	public int getMaxXmppMessageSize()
	{
		return maxXmppMessageSize;
	}

	public String getAppId()
	{
		return appId;
	}

	public Compressor getCompressor()
	{
		return compressor;
	}

	public List<String> getIgnorePatterns()
	{
		return ignorePatterns;
	}
	
	public String getAuthErrorPage()
	{
		return authErrorPage;
	}

	private List<String> ignorePatterns = new ArrayList<String>(); 
	
	private static XmlConfig instance = null;
	
	public static XmlConfig getInstance()
	{
		return instance;
	}
	
	private XmlConfig()
	{
		
	}
	
	public static XmlConfig init(ServletConfig config) throws SAXException, IOException, ParserConfigurationException
	{
		instance = new XmlConfig();
		InputStream is = config.getServletContext().getResourceAsStream("/WEB-INF/appengine-web.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		NodeList nodes = doc.getElementsByTagName("application");
		String appid = nodes.item(0).getTextContent();
		is.close();
		instance.appId = appid;
		
		is = config.getServletContext().getResourceAsStream("/WEB-INF/hyk-proxy-server.xml");
		doc = builder.parse(is);
		String compressorName = doc.getElementsByTagName("compressor").item(0).getTextContent();
		instance.compressor = CompressorFactory.getRegistCompressor(compressorName).compressor;
		
		NodeList ignoreList = doc.getElementsByTagName("content-type");
		for(int i = 0; i < ignoreList.getLength(); i++)
		{
			String ignoreType = ignoreList.item(i).getTextContent().trim();
			instance.ignorePatterns.add(ignoreType.toLowerCase());
		}
		
		String maxMessageSize = doc.getElementsByTagName("MaxMessageSize").item(0).getTextContent();
		instance.maxXmppMessageSize = Integer.parseInt(maxMessageSize);
		
		instance.httpDownStreamEncrypter = doc.getElementsByTagName("httpDownStreamEncrypter").item(0).getTextContent();
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>");
		buffer.append("<title>Error 403 User not allowed to visit this site</title>");
		buffer.append("</head>");
		buffer.append("<body><h2>HTTP ERROR 403</h2>");
		buffer.append(doc.getElementsByTagName("error-info").item(0).getTextContent());
		buffer.append("</body></html>");
		String bodystr = buffer.toString();
		instance.authErrorPage = bodystr;
		instance.isMasterNode = instance.appId.equals(Constants.MASTER_APPID);
		return instance;
	}
}
