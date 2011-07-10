/**
 * 
 */
package com.hyk.proxy.client.application.gae.rpc;

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

import org.hyk.proxy.android.config.Config.XmppAccount;
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

import com.hyk.codec.Base64;
import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.xmpp.XmppAddress;
import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.rpc.core.transport.impl.AbstractDefaultRpcChannel;

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
		setMaxMessageSize(40960);
		this.address = new XmppAddress(account.jid);
		ConnectionConfiguration connConfig = account.connectionConfig;
		xmppConnection = new XMPPConnection(connConfig);
		xmppConnection.connect();
		connConfig.setTruststoreType("bks");
		//SASLAuthentication.supportSASLMechanism("PLAIN", 0);
		xmppConnection.login(account.name, account.passwd, Constants.PROJECT_NAME);
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
	protected RpcChannelData recv() throws IOException
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
		XmppAddress xmppAddress = (XmppAddress)data.address;
		if(!xmppConnection.getRoster().contains(xmppAddress.getJid()))
		{
			Presence presence = new Presence(Presence.Type.subscribe);
			presence.setFrom(this.address.getJid());
			presence.setTo(xmppAddress.getJid());
			xmppConnection.sendPacket(presence);
		}
		Chat chat = null;
		synchronized(chatTable)
		{
			chat = chatTable.get(xmppAddress.getJid());
			if(null == chat)
			{
				chat = xmppConnection.getChatManager().createChat(xmppAddress.getJid(), this);
				chatTable.put(xmppAddress.getJid(), chat);
			}
		}
		try
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Send message from " + this.address + " to " + xmppAddress.toPrintableString());
			}
			semaphore.acquire();
			//chat.sendMessage(Base64.byteArrayBufferToBase64(data.content));
			chat.sendMessage(Base64.encodeToString(ChannelDataBuffer.asByteArray(data.content), false));
			Thread.sleep(1000);
		}
		catch(XMPPException e)
		{
			logger.error("Failed to send XMPP message", e);
			throw new IOException("Failed to send XMPP message");
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
			byte[] raw = Base64.decodeFast(content);
			//ByteArray buffer = Base64.base64ToByteArrayBuffer(content);
			ChannelDataBuffer buffer = ChannelDataBuffer.wrap(raw);
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
