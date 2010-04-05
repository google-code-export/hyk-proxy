/**
 * 
 */
package com.hyk.proxy.gae.client.rpc;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Message.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.client.config.XmppAccount;
import com.hyk.proxy.gae.common.xmpp.XmppAddress;
import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.transport.AbstractDefaultRpcChannel;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.util.buffer.ByteArray;
import com.hyk.util.codec.Base64;

/**
 * @author yinqiwen
 * 
 */
public class XmppRpcChannel extends AbstractDefaultRpcChannel implements MessageListener
{
	protected Logger				logger		= LoggerFactory.getLogger(getClass());

	private XMPPConnection					xmppConnection;
	private Map<String, Chat>				chatTable	= new HashMap<String, Chat>();
	private XmppAddress				address;
	private List<RpcChannelData>	recvList	= new LinkedList<RpcChannelData>();
	private Semaphore semaphore = new Semaphore(1);
	
	private static ScheduledExecutorService	resendService	= new ScheduledThreadPoolExecutor(2);
	
	public XmppRpcChannel(Executor threadPool, XmppAccount account) throws XMPPException
	{
		super(threadPool);
		this.address = new XmppAddress(account.getJid());
		ConnectionConfiguration connConfig = account.getConnectionConfig();
		xmppConnection = new XMPPConnection(connConfig);
		xmppConnection.connect();
		xmppConnection.login(account.getName(), account.getPasswd(), "smack");
		Presence presence = new Presence(Presence.Type.available);
		xmppConnection.sendPacket(presence);
		super.start();
	}

	@Override
	public void close()
	{
		super.close();
		xmppConnection.disconnect();
	}
	
	@Override
	public Address getRpcChannelAddress()
	{
		return address;
	}

	@Override
	protected RpcChannelData read() throws IOException
	{
		synchronized(recvList)
		{
			if(recvList.isEmpty())
			{
				try
				{
					recvList.wait();
				}
				catch(InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
			return recvList.remove(0);
		}

	}

	@Override
	protected void send(RpcChannelData data) throws IOException
	{
		XmppAddress address = (XmppAddress)data.address;
		if(!xmppConnection.getRoster().contains(address.getJid()))
		{
			Presence presence = new Presence(Presence.Type.subscribe);
			presence.setFrom(this.address.getJid());
			presence.setTo(address.getJid());
			xmppConnection.sendPacket(presence);
		}
		Chat chat = null;
		synchronized(chatTable)
		{
			chat = chatTable.get(address.getJid());
			if(null == chat)
			{
				chat = xmppConnection.getChatManager().createChat(address.getJid(), this);
				chatTable.put(address.getJid(), chat);
			}
		}
		try
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Send message from " + this.address + " to " + address.toPrintableString());
			}
			semaphore.acquire();
			chat.sendMessage(Base64.byteArrayBufferToBase64(data.content));
			Thread.sleep(1000);
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
			semaphore.release();
		}
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
			ByteArray buffer = Base64.base64ToByteArrayBuffer(content);
			RpcChannelData recv = new RpcChannelData(buffer, new XmppAddress(jid));
			synchronized(recvList)
			{
				recvList.add(recv);
				recvList.notify();
			}
		}
		else
		{
			logger.error("Receive message:" + message.getType() + ", error" + message.getError());
			resendService.schedule(new Runnable()
			{
				
				@Override
				public void run()
				{
					try
					{
						chat.sendMessage(message.getBody());
					}
					catch(XMPPException e)
					{
						logger.error("Failed to send XMPP message", e);
					}
				}
			}, 5000, TimeUnit.MICROSECONDS);
		}
	}

	@Override
	public boolean isReliable()
	{
		return true;
	}

}
