/**
 * 
 */
package com.hyk.proxy.gae.server.core;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.hyk.compress.CompressorFactory;
import com.hyk.compress.CompressorType;
import com.hyk.proxy.gae.common.HttpServerAddress;
import com.hyk.proxy.gae.server.core.rpc.HttpServletRpcChannel;
import com.hyk.proxy.gae.server.core.rpc.XmppServletRpcChannel;
import com.hyk.proxy.gae.server.core.service.FetchServiceImpl;
import com.hyk.proxy.gae.server.core.util.HttpMessageCompressPreference;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.constant.RpcConstants;

/**
 * @author yinqiwen
 *
 */
public class Launcher extends HttpServlet{

	protected Logger								logger			= LoggerFactory.getLogger(getClass());
	
	private static XmppServletRpcChannel xmppServletRpcChannel = null;
	private static HttpServletRpcChannel httpServletRpcChannel = null;
	
	private static String appid;
	
	
	public static XmppServletRpcChannel getXmppServletRpcChannel()
	{
		return xmppServletRpcChannel;
	}
	
	public static HttpServletRpcChannel getHttpServletRpcChannel()
	{
		return httpServletRpcChannel;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try
		{	
			InputStream is = config.getServletContext().getResourceAsStream("/WEB-INF/appengine-web.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			NodeList nodes = doc.getElementsByTagName("application");
			appid = nodes.item(0).getTextContent();
			is.close();
			
			String type = config.getInitParameter("remoteserver.rpc.compressor.type").trim();
			String trigger = config.getInitParameter("remoteserver.rpc.compressor.trigger").trim();
			
			Properties initProps = new Properties();
			initProps.setProperty(RpcConstants.TIMER_CLASS, "com.hyk.proxy.gae.server.core.util.AppEngineTimer");
			initProps.setProperty(RpcConstants.COMPRESS_PREFER, "com.hyk.proxy.gae.server.core.util.HttpMessageCompressPreference");
			HttpMessageCompressPreference.init(CompressorFactory.getCompressor(CompressorType.valueOfName(type)), Integer.parseInt(trigger));
			
			XmppServletRpcChannel transport = new XmppServletRpcChannel(appid + "@appspot.com");
			xmppServletRpcChannel = transport;	
			RPC xmppRpc = new RPC(transport, initProps);
			xmppRpc.getLocalNaming().bind("fetch", new FetchServiceImpl());
			
			httpServletRpcChannel = new HttpServletRpcChannel(new HttpServerAddress(appid + ".appspot.com", "/fetchproxy"));
			RPC httpRpc = new RPC(httpServletRpcChannel, initProps);
			httpServletRpcChannel.setMaxMessageSize(10240000);
			httpRpc.getLocalNaming().bind("fetch", new FetchServiceImpl());
			
			if(logger.isInfoEnabled())
			{
				logger.info("Launcher init!");
			}
		}
		catch(Exception e)
		{
			logger.error("Error occured when init launch servlet!", e);
		}
		
		
	}
}
