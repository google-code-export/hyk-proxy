/**
 * 
 */
package org.hyk.proxy.gae.client.connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.arch.buffer.Buffer;
import org.arch.misc.crypto.base64.Base64;
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
public class XMPPProxyConnection extends ProxyConnection implements MessageListener
{
	protected Logger				logger		= LoggerFactory.getLogger(getClass());

	private XMPPConnection					xmppConnection;
	private Map<String, Chat>				chatTable	= new HashMap<String, Chat>();
	private XmppAddress				address;
	private XmppAddress serverAddress;
	
	private static ScheduledExecutorService	resendService	= new ScheduledThreadPoolExecutor(2);
	
	public XMPPProxyConnection(XmppAccount account, XmppAddress serverAddr) throws XMPPException
	{
		this.address = new XmppAddress(account.jid);
		this.serverAddress = serverAddr;
		ConnectionConfiguration connConfig = account.connectionConfig;
		xmppConnection = new XMPPConnection(connConfig);
		xmppConnection.connect();
		xmppConnection.login(account.name, account.passwd, Constants.PROJECT_NAME);
		Presence presence = new Presence(Presence.Type.available);
		xmppConnection.sendPacket(presence);
	}

	public void close()
	{
		xmppConnection.disconnect();
	}

	public void processMessage(final Chat chat, final Message message)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Recv message from " + message.getFrom() + " to " + message.getTo());
		}
		
		if(message.getType().equals(Type.chat))
		{
			String jid = message.getFrom();
			String content = message.getBody();
			byte[] raw = Base64.decodeFast(content);
			
		}
		else
		{
			logger.error("Receive message:" + message.getType() + ", error" + message.getError());
//			resendService.schedule(new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					try
//					{
//						chat.sendMessage(message.getBody());
//					}
//					catch(XMPPException e)
//					{
//						logger.error("Failed to send XMPP message", e);
//					}
//				}
//			}, 5000, TimeUnit.MICROSECONDS);
		}
	}

	@Override
    protected boolean doSend(Buffer msgbuffer)
    {
		//XmppAddress xmppAddress = null;
		if(!xmppConnection.getRoster().contains(serverAddress.getJid()))
		{
			Presence presence = new Presence(Presence.Type.subscribe);
			presence.setFrom(this.address.getJid());
			presence.setTo(serverAddress.getJid());
			xmppConnection.sendPacket(presence);
		}
		Chat chat = null;
		synchronized(chatTable)
		{
			chat = chatTable.get(serverAddress.getJid());
			if(null == chat)
			{
				chat = xmppConnection.getChatManager().createChat(serverAddress.getJid(), this);
				chatTable.put(serverAddress.getJid(), chat);
			}
		}
		try
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Send message from " + this.address + " to " + serverAddress.toPrintableString());
			}
			//semaphore.acquire();
			
			//chat.sendMessage(Base64.byteArrayBufferToBase64(data.content));
			chat.sendMessage(Base64.encodeToString(ChannelDataBuffer.asByteArray(data.content), false));
			//Thread.sleep(1000);
		}
		catch(XMPPException e)
		{
			logger.error("Failed to send XMPP message", e);
			throw new IOException(e);
		}
		catch(Exception e)
		{
			logger.error("Failed to send XMPP message", e);
		}
		finally
		{
			//semaphore.release();
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
