/**
 * 
 */
package com.hyk.rpc.core.message;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.hyk.rpc.core.address.Address;
import com.hyk.serializer.Externalizable;
import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 * @author qiying.wang
 * 
 */
public class Message implements Externalizable
{
	// protected transient Address address;
	// protected transient long sessionID;
	protected transient MessageID	id	= new MessageID();

	public MessageID getId()
	{
		return id;
	}

	public void setId(MessageID id)
	{
		this.id = id;
	}

	protected MessageType			 type;
	protected AbstractMessageObject	 value;

	public Address getAddress()
	{
		return id.address;
	}

	public void setAddress(Address address)
	{
		id.address = address;
	}

	public long getSessionID()
	{
		return id.sessionID;
	}

	public void setSessionID(long sessionID)
	{
		id.sessionID = sessionID;
	}

	public MessageType getType()
	{
		return type;
	}

	public AbstractMessageObject getValue()
	{
		return value;
	}

	@Override
	public void readExternal(SerializerInput in) throws IOException
	{
		int v = in.readInt();
		type = MessageType.valueOf(v);

		switch(type)
		{
			case Request:
			{
				value = in.readObject(Request.class);
				break;
			}
			case Response:
			{
				value = in.readObject(Response.class);			
				break;
			}
			default:
			{
				// System.out.println("####" + v);
				break;
			}
		}
	}

	@Override
	public void writeExternal(SerializerOutput out) throws IOException
	{
		out.writeInt(type.getValue());
		out.writeObject(value);
	}

}
