package com.hyk.proxy.gae.server.config;

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

import com.hyk.compress.Compressor;
import com.hyk.compress.CompressorFactory;
import com.hyk.compress.CompressorType;

public class Config
{
	private String appId;
	private Compressor compressor;
	private int compressTrigger;
	
	private String blacklistErrorInfo;

	private int maxXmppMessageSize;
	
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

	public int getCompressTrigger()
	{
		return compressTrigger;
	}

	public List<Pattern> getIgnorePatterns()
	{
		return ignorePatterns;
	}
	
	public String getBlacklistErrorInfo()
	{
		return blacklistErrorInfo;
	}

	private List<Pattern> ignorePatterns = new ArrayList<Pattern>(); 
	
	private static Config instance = null;
	
	public static Config getInstance()
	{
		return instance;
	}
	
	private Config()
	{
		
	}
	
	public static Config init(ServletConfig config) throws SAXException, IOException, ParserConfigurationException
	{
		instance = new Config();
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
		String compressorType = doc.getElementsByTagName("compressor").item(0).getTextContent();
		String compressorTrigger = doc.getElementsByTagName("trigger").item(0).getTextContent();
		instance.compressor = CompressorFactory.getCompressor(CompressorType.valueOfName(compressorType));
		instance.compressTrigger = Integer.parseInt(compressorTrigger);
		
		NodeList ignoreList = doc.getElementsByTagName("content-type");
		for(int i = 0; i < ignoreList.getLength(); i++)
		{
			String ignoreType = ignoreList.item(i).getTextContent().trim();
			instance.ignorePatterns.add(Pattern.compile(ignoreType.toLowerCase()));
		}
		
		String maxMessageSize = doc.getElementsByTagName("MaxMessageSize").item(0).getTextContent();
		instance.maxXmppMessageSize = Integer.parseInt(maxMessageSize);
		
		instance.blacklistErrorInfo = doc.getElementsByTagName("error-info").item(0).getTextContent();
		return instance;
	}
}
