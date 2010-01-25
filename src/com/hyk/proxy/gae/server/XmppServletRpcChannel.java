/**
 * 
 */
package com.hyk.proxy.gae.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.hyk.proxy.gae.common.XmppAddress;
import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.message.MessageFragment;
import com.hyk.rpc.core.message.MessageID;
import com.hyk.rpc.core.transport.RpcChannel;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.util.buffer.ByteArray;
import com.hyk.util.codec.Base64;

/**
 * @author Administrator
 * 
 */
public class XmppServletRpcChannel extends RpcChannel
{

	XMPPService						xmpp		= XMPPServiceFactory.getXMPPService();
	MemcacheService					memcache	= MemcacheServiceFactory.getMemcacheService();
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
	protected void deleteMessageFragments(MessageID id)
	{
		memcache.delete(id);
	}


	@Override
	public Address getRpcChannelAddress()
	{
		// TODO Auto-generated method stub
		return address;
	}

	
	@Override
	protected MessageFragment[] loadMessageFragments(MessageID id)
	{
		return (MessageFragment[])memcache.get(id);

	}

	@Override
	protected RpcChannelData read() throws IOException
	{
		throw new IOException("Not supported!");
	}

	@Override
	protected void saveMessageFragment(MessageFragment fragment)
	{
		MessageFragment[] fragments = (MessageFragment[])memcache.get(fragment.getId());
		if(null == fragments)
		{
			fragments = new MessageFragment[fragment.getTotalFragmentCount()];
		}
		fragments[fragment.getSequence()] = fragment;
		memcache.put(fragment.getId(), fragments);
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

	public void processXmppMessage(Message msg) throws Exception
	{	
		JID fromJid = msg.getFromJid();
		int index = fromJid.getId().indexOf('/');
		String jid =  fromJid.getId().substring(0, index);
		ByteArray buffer = Base64.base64ToByteArrayBuffer(msg.getBody());
		RpcChannelData recv = new RpcChannelData(buffer, new XmppAddress(jid));
		processIncomingData(recv);
//		synchronized(recvList)
//		{
//			recvList.add(recv);
//			recvList.notify();
//		}
	}
}
