package org.hyk.proxy.gae.server.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventDispatcher;
import org.arch.misc.crypto.base64.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

@SuppressWarnings("serial")
public class XmppInvokeServlet extends HttpServlet
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected XMPPService xmpp = XMPPServiceFactory.getXMPPService();

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	        throws IOException
	{
		Message message = xmpp.parseMessage(req);

		try
		{
			JID jid = message.getFromJid();
			byte[] raw = Base64.decodeFast(message.getBody());
			Buffer buffer = Buffer.wrapReadableContent(raw);
			Event event = EventDispatcher.getSingletonInstance().parse(buffer);
			event.setAttachment(jid);
			EventDispatcher.getSingletonInstance().dispatch(event);
		}
		catch (Throwable e)
		{
			logger.warn("Failed to process message", e);
		}

	}
}
