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
public class XmppServletRpcChannel extends RpcChannel {

	XMPPService xmpp = XMPPServiceFactory.getXMPPService();
	MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
	private List<RpcChannelData> recvList = new LinkedList<RpcChannelData>();
	private static final int RETRY = 10;
	public XmppServletRpcChannel(Executor threadPool) {
		super(threadPool);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void deleteMessageFragments(MessageID id) {
		memcache.delete(id);
	}

	/* (non-Javadoc)
	 * @see com.hyk.rpc.core.transport.RpcChannel#getRpcChannelAddress()
	 */
	@Override
	public Address getRpcChannelAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.hyk.rpc.core.transport.RpcChannel#loadMessageFragments(long)
	 */
	@Override
	protected MessageFragment[] loadMessageFragments(MessageID id) {
		return (MessageFragment[]) memcache.get(id);
		
	}

	/* (non-Javadoc)
	 * @see com.hyk.rpc.core.transport.RpcChannel#read()
	 */
	@Override
	protected RpcChannelData read() throws IOException {
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

	/* (non-Javadoc)
	 * @see com.hyk.rpc.core.transport.RpcChannel#saveMessageFragment(com.hyk.rpc.core.message.MessageFragment)
	 */
	@Override
	protected void saveMessageFragment(MessageFragment fragment) {
		MessageFragment[] fragments = (MessageFragment[]) memcache.get(fragment.getId());
		if(null == fragments)
		{
			fragments = new MessageFragment[fragment.getTotalFragmentCount()];
		}
		fragments[fragment.getSequence()] = fragment;
		memcache.put(fragment.getId(), fragments);
	}

	/* (non-Javadoc)
	 * @see com.hyk.rpc.core.transport.RpcChannel#send(com.hyk.rpc.core.transport.RpcChannelData)
	 */
	@Override
	protected void send(RpcChannelData data) throws IOException {
		
		XmppAddress address = (XmppAddress) data.address;
		JID jid = new JID(address.getJid());
		Message msg = new MessageBuilder().withRecipientJids(jid).withBody(
				Base64.byteArrayBufferToBase64(data.content)).build();
		{
			int retry = RETRY;
			SendResponse status = xmpp.sendMessage(msg);
			while(status.getStatusMap().get(jid) != SendResponse.Status.SUCCESS && retry-- > 0);
		}
	}

	public void processXmppMessage(Message msg)
	{
		JID fromJid = msg.getFromJid();
		ByteArray buffer = Base64.base64ToByteArrayBuffer(msg.getBody());
		RpcChannelData recv = new RpcChannelData(buffer, new XmppAddress(fromJid.getId()));
		synchronized (recvList) {
			recvList.add(recv);
			recvList.notify();
		}
	}
}
