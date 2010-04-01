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

	private List<Pattern> ignorePatterns = new ArrayList<Pattern>(); 
	
	private Config()
	{
		
	}
	
	public static Config init(ServletConfig config) throws SAXException, IOException, ParserConfigurationException
	{
		Config ret = new Config();
		InputStream is = config.getServletContext().getResourceAsStream("/WEB-INF/appengine-web.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		NodeList nodes = doc.getElementsByTagName("application");
		String appid = nodes.item(0).getTextContent();
		is.close();
		ret.appId = appid;
		
		is = config.getServletContext().getResourceAsStream("/WEB-INF/hyk-proxy-server.xml");
		doc = builder.parse(is);
		String compressorType = doc.getElementsByTagName("compressor").item(0).getTextContent();
		String compressorTrigger = doc.getElementsByTagName("trigger").item(0).getTextContent();
		ret.compressor = CompressorFactory.getCompressor(CompressorType.valueOfName(compressorType));
		ret.compressTrigger = Integer.parseInt(compressorTrigger);
		
		NodeList ignoreList = doc.getElementsByTagName("content-type");
		for(int i = 0; i < ignoreList.getLength(); i++)
		{
			String ignoreType = ignoreList.item(i).getTextContent().trim();
			ret.ignorePatterns.add(Pattern.compile(ignoreType.toLowerCase()));
		}
		
		String maxMessageSize = doc.getElementsByTagName("MaxMessageSize").item(0).getTextContent();
		ret.maxXmppMessageSize = Integer.parseInt(maxMessageSize);
		return ret;
	}
}
