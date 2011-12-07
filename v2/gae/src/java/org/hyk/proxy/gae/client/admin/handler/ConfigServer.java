/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ConfigServer.java 
 *
 * @author yinqiwen [ 2011-12-5 | ÏÂÎç10:44:57 ]
 *
 */
package org.hyk.proxy.gae.client.admin.handler;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.arch.buffer.Buffer;
import org.hyk.proxy.gae.client.admin.GAEAdmin;
import org.hyk.proxy.gae.client.connection.ProxyConnection;
import org.hyk.proxy.gae.common.CompressorType;
import org.hyk.proxy.gae.common.EncryptType;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.config.GAEServerConfiguration;
import org.hyk.proxy.gae.common.event.ServerConfigEvent;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 */
public class ConfigServer implements CommandHandler
{
	public static final String COMMAND = "servercfg";

//	private static final String EXAMPLE = "Examples:"
//	        + System.getProperty("line.separator")
//	        + "servercfg get  #get server config  "
//	        + System.getProperty("line.separator")
//	        + "servercfg set  #set server config by gae-server.xml."
//	        + System.getProperty("line.separator");
	private Options options = new Options();

	private ProxyConnection connection;

	public ConfigServer(ProxyConnection connection)
	{
		this.connection = connection;
		options.addOption("h", "help", false, "print this message.");
	}

	
	private static GAEServerConfiguration buildFromXML() throws Exception
	{
		GAEServerConfiguration cfg = new GAEServerConfiguration();
		GAEServerConfiguration.class.getResource("/"
                + GAEConstants.SERVER_CONF_NAME);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream is = GAEServerConfiguration.class.getResourceAsStream("/"
                + GAEConstants.SERVER_CONF_NAME);
		Document doc = builder.parse(is);
		is.close();
		NodeList filters = doc.getElementsByTagName("content-type");
		HashSet filterset = new HashSet<String>();
		for(int i = 0; i<filters.getLength(); i++)
		{
			
			filterset.add(filters.item(i).getTextContent().trim());
		}
		cfg.setCompressFilter(filterset);
		NodeList maxxmpp = doc.getElementsByTagName("MaxXMPPDataPackageSize");
		NodeList fetchRetry = doc.getElementsByTagName("FetchRetryCount");
		NodeList rangeLimit = doc.getElementsByTagName("RangeFetchLimit");
		NodeList compressor = doc.getElementsByTagName("Compressor");
		NodeList encrypter = doc.getElementsByTagName("Encrypter");
		cfg.setMaxXMPPDataPackageSize(Integer.parseInt(maxxmpp.item(0).getTextContent().trim()));
		cfg.setFetchRetryCount(Integer.parseInt(fetchRetry.item(0).getTextContent().trim()));
		cfg.setRangeFetchLimit(Integer.parseInt(rangeLimit.item(0).getTextContent().trim()));
		cfg.setCompressor(CompressorType.valueOf(compressor.item(0).getTextContent().trim().toUpperCase()));
		cfg.setEncrypter(EncryptType.valueOf(encrypter.item(0).getTextContent().trim().toUpperCase()));
		return cfg;
	}
	
//	public static void main(String[] args) throws Exception
//	{
//		ConfigServerEvent event = new ConfigServerEvent();
//		event.cfg = buildFromXML();
//		Buffer buffer = new Buffer();
//		buffer.ensureWritableBytes(1024);
//		event.encode(buffer);
//		System.out.println(buffer.readableBytes());
//	}
	
	@Override
	public void execute(String[] args)
	{
		CommandLineParser parser = new PosixParser();

		try
		{
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			// validate that block-size has been set
			if (line.hasOption("h"))
			{
				printHelp();
			}
			else
			{
				String[] oprargs = line.getArgs();
				if (oprargs != null && oprargs.length > 1)
				{
					System.out.println(oprargs.length);
					GAEAdmin.outputln("Only one arg expected!"
					        + Arrays.toString(oprargs));
				}
				else
				{
					String opr = "get";
					if (oprargs != null && oprargs.length == 1)
					{
						opr = oprargs[0];
					}
					opr = opr.toLowerCase();
					if(!opr.equals("get") && !opr.equals("set"))
					{
						GAEAdmin.errln("Only get/set parameter is valid");
						return;
					}
					
					ServerConfigEvent event = new ServerConfigEvent();
					if(opr.equals("get"))
					{
						event.opreration = ServerConfigEvent.GET_CONFIG_REQ;
					}
					else
					{
						event.opreration = ServerConfigEvent.SET_CONFIG_REQ;
						event.cfg = buildFromXML();
					}
					AdminResponseEventHandler.syncSendEvent(connection, event);
				}

			}

		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			System.out.println("Error:" + exp.getMessage());
		}

	}

	@Override
	public void printHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(COMMAND + " [get|set]", options);

	}

}
