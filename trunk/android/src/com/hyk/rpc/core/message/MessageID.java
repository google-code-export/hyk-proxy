/**
 * 
 */
package com.hyk.rpc.core.message;

import java.io.Serializable;

import com.hyk.rpc.core.address.Address;

//import com.hyk.rpc.core.session.SessionManager.ServerSessionID;

/**
 * @author Administrator
 * 
 */
public class MessageID implements Serializable
{
	@Override
	public String toString()
	{
		return "" + sessionID;
	}

	transient Address	address;
	long				sessionID;

	public MessageID()
	{
	}

	public MessageID(Address address, long sessionID)
	{
		this.address = address;
		this.sessionID = sessionID;
	}

	public boolean equals(Object anObject)
	{
		if(this == anObject)
		{
			return true;
		}
		if(anObject instanceof MessageID)
		{
			MessageID anotherServerSessionID = (MessageID)anObject;

			return anotherServerSessionID.sessionID == sessionID && anotherServerSessionID.address.equals(address);
		}
		return false;
	}

	public int hashCode()
	{
		return (int)(sessionID ^ (sessionID >>> 32)) + address.hashCode();
	}
}
