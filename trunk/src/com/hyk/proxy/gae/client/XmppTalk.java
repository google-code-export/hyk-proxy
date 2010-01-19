/**
 * 
 */
package com.hyk.proxy.gae.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;
import com.hyk.proxy.gae.common.XmppMeaageUtil;
import com.hyk.proxy.gae.common.XmppMeaageUtil.HykProxyXmppResponse;

/**
 * @author Administrator
 * 
 */
public class XmppTalk {

	private static AtomicInteger sessionIDSeed = new AtomicInteger(1);

	private static Map<Integer, XmppTalk> xmppTalkerTable = new ConcurrentHashMap<Integer, XmppTalk>();

	private static XMPPConnection xmppConnection;
	private static Chat chat;

	private static class ResponseMessageListener implements MessageListener {

		@Override
		public void processMessage(Chat chat, Message message) {
			// System.out.println("####processMessage! ");
			HykProxyXmppResponse response = XmppMeaageUtil
					.parseResponse(message.getBody());
			XmppTalk talker = xmppTalkerTable.get(response.sessionId);
			if (null != talker) {
				// System.out.println("####forward processMessage! ");
				talker.processResponse(response);
			} else {
				System.out.println("####discard message! " + response.body);
			}
		}

	}

	private List<HykProxyXmppResponse> resBuffer = new LinkedList<HykProxyXmppResponse>();
	private int resSeqNum = 0;
	private String response;
	private String exception;

	static {
		try {
			ConnectionConfiguration connConfig = new ConnectionConfiguration(
			// "talk.google.com", 5222, "gmail.com");
					"jabber.org", 5222, "jabber.org");
			//connConfig.setCompressionEnabled(true);
			//connConfig.
			// connConfig.setDebuggerEnabled(true);
			xmppConnection = new XMPPConnection(connConfig);
			xmppConnection.connect();
			// xmppConnection.login("yinqiwen@gmail.com", "Kingwon1983",
			// "smack");
			xmppConnection.login("hykproxy", "fuckgfw", "smack");
			Presence presence = new Presence(Presence.Type.available);
			xmppConnection.sendPacket(presence);

			//xmppConnection.getChatManager().
			chat = xmppConnection.getChatManager().createChat(
					"hykserver@appspot.com", new ResponseMessageListener());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public byte[] talk(byte[] req) throws XMPPException, InterruptedException,
			Base64DecoderException, TalkException {

		StringBuffer msgBuffer = new StringBuffer();
		int sessionID = sessionIDSeed.getAndIncrement();
		xmppTalkerTable.put(sessionID, this);
		try {
			msgBuffer.append("[").append(sessionID).append("]");
			msgBuffer.append(Base64.encode(req));
			// Chat chat = xmppConnection.getChatManager().createChat(
			// "hykserver@appspot.com", this);
			// chat.
			chat.sendMessage(msgBuffer.toString());
			synchronized (this) {
				this.wait(120000);
			}
			if(null != exception)
			{
				throw new TalkException(400, exception);
			}
			if (null == response) {
				throw new TalkException(408,"Timeout!");
			}
			return Base64.decode(response);
		} finally {
			xmppTalkerTable.remove(sessionID);
		}

	}

	private void buildResponse() {
		//System.out.println("####buildResponse! ");
		HykProxyXmppResponse[] ress = new HykProxyXmppResponse[resBuffer.size()];
		resBuffer.toArray(ress);
		Arrays.sort(ress);
		StringBuffer buffer = new StringBuffer();
		for (HykProxyXmppResponse res : ress) {
			buffer.append(res.body);
		}

		response = buffer.toString();
	}

	public void processResponse(HykProxyXmppResponse res) {
		if(res.seq == 0)
		{
			exception = res.body;
			synchronized (this) {
				this.notify();
			}
		}
		
		if (res.seq > 0) {
			resBuffer.add(res);
		} else {
			if (resSeqNum > 0) {
				System.out.println("####already set" + resSeqNum);
			}
			resSeqNum = -res.seq;
		}
		// System.out.println("####" + resSeqNum + " == " + resBuffer.size() +
		// " /" + body.substring(0,5));
		if (resBuffer.size() == resSeqNum) {
			buildResponse();
			synchronized (this) {
				this.notify();
			}
		}

	}
}
