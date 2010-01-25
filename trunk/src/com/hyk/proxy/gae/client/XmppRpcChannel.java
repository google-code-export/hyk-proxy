/**
 * 
 */
package com.hyk.proxy.gae.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
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
public class XmppRpcChannel extends AbstractDefaultRpcChannel implements MessageListener {
	protected Logger								logger			= LoggerFactory.getLogger(getClass());
	
	XMPPConnection xmppConnection;
	Map<String, Chat> chatTable = new HashMap<String, Chat>();
	//Chat chat;
	private XmppAddress address;
	private List<RpcChannelData> recvList = new LinkedList<RpcChannelData>();
	public XmppRpcChannel(Executor threadPool, String jid) throws XMPPException {
		super(threadPool);
		this.address = new XmppAddress(jid);
		ConnectionConfiguration connConfig = new ConnectionConfiguration(
				"talk.google.com", 5222, "gmail.com");
		xmppConnection = new XMPPConnection(connConfig);
		xmppConnection.connect();
		xmppConnection.login("yinqiwen@gmail.com", "Kingwon1983", "smack");
		Presence presence = new Presence(Presence.Type.available);
		xmppConnection.sendPacket(presence);
		super.start();
	}

	@Override
	public Address getRpcChannelAddress() {
		return address;
	}

	@Override
	protected RpcChannelData read() throws IOException{
		synchronized (recvList) {
			if(recvList.isEmpty())
			{
				try {
					recvList.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
			return recvList.remove(0);
		}
		
	}

	@Override
	protected void send(RpcChannelData data) throws IOException{
		XmppAddress address = (XmppAddress) data.address;
		Chat chat = null;
		synchronized (chatTable) {
			chat = chatTable.get(address.getJid());
			if(null == chat)
			{
				chat = xmppConnection.getChatManager().createChat(
						address.getJid(), this);
				chatTable.put(address.getJid(), chat);
			}
		}
		try {
			if(logger.isDebugEnabled())
			{
				logger.debug("Send message to " + address.toPrintableString());
			}
			chat.sendMessage(Base64.byteArrayBufferToBase64(data.content));
		} catch (XMPPException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	public void processMessage(Chat chat, Message message) {
		if(logger.isDebugEnabled())
		{
			logger.debug("Recv message from " + message.getFrom());
		}
		String jid = message.getFrom();
		int index = jid.indexOf('/');
		jid = jid.substring(0, index);
		String content = message.getBody();
		ByteArray buffer = Base64.base64ToByteArrayBuffer(content);
		RpcChannelData recv = new RpcChannelData(buffer, new XmppAddress(jid));
		synchronized (recvList) {
			recvList.add(recv);
			recvList.notify();
		}
	}

}
