/**
 * 
 */
package com.hyk.proxy.gae.client.xmpp;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.common.XmppAddress;
import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.transport.AbstractDefaultRpcChannel;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.util.buffer.ByteArray;
import com.hyk.util.codec.Base64;

/**
 * @author Administrator
 * 
 */
public class XmppRpcChannel extends AbstractDefaultRpcChannel implements MessageListener
{
	protected Logger				logger		= LoggerFactory.getLogger(getClass());
	
	private static final int DEFAULT_PORT = 5222;
	private static final String GTALK_SERVER = "talk.google.com";
	private static final String GMAIL = "gmail.com";

	private XMPPConnection					xmppConnection;
	private Map<String, Chat>				chatTable	= new HashMap<String, Chat>();
	private XmppAddress				address;
	private List<RpcChannelData>	recvList	= new LinkedList<RpcChannelData>();
	
	public XmppRpcChannel(Executor threadPool, String jid, String passwd) throws XMPPException
	{
		super(threadPool);
		this.address = new XmppAddress(jid);
		String server = StringUtils.parseServer(jid);
		String serviceName = server;
		String user = jid;
		if(server.equals(GMAIL))
		{
			server = GTALK_SERVER;
		}
		else
		{
			user =  StringUtils.parseName(jid);
		}
		ConnectionConfiguration connConfig = new ConnectionConfiguration(server, DEFAULT_PORT,serviceName);
		//connConfig.setDebuggerEnabled(true);
		xmppConnection = new XMPPConnection(connConfig);
		xmppConnection.connect();
		xmppConnection.login(user, passwd, "smack");
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
				logger.debug("Send message to " + address.toPrintableString());
			}
			chat.sendMessage(Base64.byteArrayBufferToBase64(data.content));
		}
		catch(XMPPException e)
		{
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	public void processMessage(Chat chat, Message message)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Recv message from " + message.getFrom());
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
			//maybe retry sending request is better
		}
	}

	@Override
	public boolean isReliable()
	{
		return true;
	}

}
