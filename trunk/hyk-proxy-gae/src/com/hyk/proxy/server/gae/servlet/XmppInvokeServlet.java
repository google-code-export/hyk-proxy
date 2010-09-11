package com.hyk.proxy.server.gae.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

@SuppressWarnings("serial")
public class XmppInvokeServlet extends HttpServlet
{
	protected Logger	logger	= LoggerFactory.getLogger(getClass());
	protected XMPPService xmpp = XMPPServiceFactory.getXMPPService();

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		Message message = xmpp.parseMessage(req);

		if(logger.isInfoEnabled())
		{
			logger.info("Process message from " + message.getFromJid());
		}

		try
		{
			Launcher.getXmppServletRpcChannel().processXmppMessage(message);
		}
		catch(Throwable e)
		{
			logger.warn("Failed to process message", e);
		}

	}
}
