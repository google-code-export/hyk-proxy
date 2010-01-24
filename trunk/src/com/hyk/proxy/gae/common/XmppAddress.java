/**
 * 
 */
package com.hyk.proxy.gae.common;

import com.hyk.rpc.core.address.Address;

/**
 * @author Administrator
 *
 */
public class XmppAddress implements Address {

	private String jid;
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public XmppAddress()
	{
		
	}
	
	public XmppAddress(String jid)
	{
		this.jid = jid;
	}
}
