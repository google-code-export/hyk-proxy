/**
 * 
 */
package org.hyk.proxy.gae.client.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.arch.buffer.Buffer;
import org.arch.misc.crypto.base64.Base64;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.GAEServerAuth;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.XmppAccount;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.xmpp.XmppAddress;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 * 
 */
public class XMPPProxyConnection extends ProxyConnection implements
        MessageListener
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private static Map<String, XMPPConnection> rawConnectionTable = new HashMap<String, XMPPConnection>();
	private XMPPConnection xmppConnection;
	private Map<String, Chat> chatTable = new HashMap<String, Chat>();
	private XmppAddress address;
	private XmppAddress serverAddress;

	private static ScheduledExecutorService resendService = new ScheduledThreadPoolExecutor(
	        2);

	public XMPPProxyConnection(GAEServerAuth auth, XmppAccount account)
	        throws XMPPException
	{
		super(auth);
		this.address = new XmppAddress(account.jid);
		this.serverAddress = new XmppAddress(auth.appid + "@appspot.com");
		ConnectionConfiguration connConfig = account.connectionConfig;
		if (rawConnectionTable.containsKey(account.jid))
		{
			xmppConnection = rawConnectionTable.get(account.jid);
		}
		else
		{
			xmppConnection = new XMPPConnection(connConfig);
			xmppConnection.connect();
			xmppConnection.login(account.name, account.passwd,
			        GAEConstants.XMPP_CLIENT_NAME);
			Presence presence = new Presence(Presence.Type.available);
			xmppConnection.sendPacket(presence);
			rawConnectionTable.put(account.jid, xmppConnection);
		}
	}

	public void close()
	{
		xmppConnection.disconnect();
		rawConnectionTable.remove(address.getJid());
	}

	public void processMessage(final Chat chat, final Message message)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("Recv message from " + message.getFrom() + " to "
			        + message.getTo());
		}

		if (message.getType().equals(Type.chat))
		{
			String jid = message.getFrom();
			String content = message.getBody();
			byte[] raw = Base64.decodeFast(content);
			Buffer buffer = Buffer.wrapReadableContent(raw);
			doRecv(buffer);
		}
		else
		{
			logger.error("Receive message:" + message.getType() + ", error"
			        + message.getError());
			// resendService.schedule(new Runnable()
			// {
			// @Override
			// public void run()
			// {
			// try
			// {
			// chat.sendMessage(message.getBody());
			// }
			// catch(XMPPException e)
			// {
			// logger.error("Failed to send XMPP message", e);
			// }
			// }
			// }, 5000, TimeUnit.MICROSECONDS);
		}
	}

	@Override
	protected boolean doSend(Buffer msgbuffer)
	{
		if (!xmppConnection.getRoster().contains(serverAddress.getJid()))
		{
			Presence presence = new Presence(Presence.Type.subscribe);
			presence.setFrom(this.address.getJid());
			presence.setTo(serverAddress.getJid());
			xmppConnection.sendPacket(presence);
		}
		Chat chat = null;
		synchronized (chatTable)
		{
			chat = chatTable.get(serverAddress.getJid());
			if (null == chat)
			{
				chat = xmppConnection.getChatManager().createChat(
				        serverAddress.getJid(), this);
				chatTable.put(serverAddress.getJid(), chat);
			}
		}
		try
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Send message from " + this.address + " to "
				        + serverAddress.toPrintableString());
			}
			// semaphore.acquire();
			// chat.sendMessage(Base64.byteArrayBufferToBase64(data.content));

			chat.sendMessage(Base64.encodeToString(msgbuffer.getRawBuffer(),
			        msgbuffer.getReadIndex(), msgbuffer.readableBytes(), false));
			// Thread.sleep(1000);
			return true;
		}
		catch (Exception e)
		{
			logger.error("Failed to send XMPP message", e);
		}
		return false;
	}

	@Override
	protected int getMaxDataPackageSize()
	{
		return GAEConstants.APPENGINE_XMPP_BODY_LIMIT;
	}

	@Override
	public boolean isReady()
	{
		return true;
	}
}
