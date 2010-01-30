/**
 * 
 */
package com.hyk.proxy.gae.server.core.rpc;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.hyk.proxy.gae.common.XmppAddress;
import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.rpc.core.transport.RpcChannelException;
import com.hyk.util.buffer.ByteArray;
import com.hyk.util.codec.Base64;

/**
 * @author Administrator
 * 
 */
public class XmppServletRpcChannel extends AbstractAppEngineRpcChannel
{

	XMPPService						xmpp		= XMPPServiceFactory.getXMPPService();
	
	//private List<RpcChannelData>	recvList	= new LinkedList<RpcChannelData>();
	private static final int		RETRY		= 10;
	private XmppAddress				address;

	public XmppServletRpcChannel(String jid)
	{
		super();
		address = new XmppAddress(jid);
		//super.start();
	}

	@Override
	public Address getRpcChannelAddress()
	{
		// TODO Auto-generated method stub
		return address;
	}

	@Override
	protected void send(RpcChannelData data) throws IOException
	{
		if(logger.isInfoEnabled())
		{
			logger.info("Send message to " + data.address.toPrintableString());
		}
		XmppAddress dataaddress = (XmppAddress)data.address;
		JID jid = new JID(dataaddress.getJid());
		Message msg = new MessageBuilder().withRecipientJids(jid).withBody(Base64.byteArrayBufferToBase64(data.content)).build();
		{
			int retry = RETRY;
			SendResponse status = xmpp.sendMessage(msg);
			while(status.getStatusMap().get(jid) != SendResponse.Status.SUCCESS && retry-- > 0)
				;
		}
	}

	public void processXmppMessage(Message msg) 
	{	
		JID fromJid = msg.getFromJid();
		String jid = fromJid.getId();
		
		try
		{
			ByteArray buffer = Base64.base64ToByteArrayBuffer(msg.getBody());
			RpcChannelData recv = new RpcChannelData(buffer, new XmppAddress(jid));
			processIncomingData(recv);
		}
		catch(Exception e)
		{
			CharArrayWriter writer = new CharArrayWriter();
			e.printStackTrace(new PrintWriter(writer));
			msg = new MessageBuilder().withRecipientJids(fromJid).withBody(writer.toString()).build();
			xmpp.sendMessage(msg);
		}

	}

	@Override
	public boolean isReliable()
	{
		return true;
	}
}
