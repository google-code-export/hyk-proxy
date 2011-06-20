/**
 * 
 */
package com.hyk.rpc.core.message;

import java.io.IOException;

import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 * @author qiying.wang
 * 
 */
public class Response extends AbstractMessageObject
{

	@Override
	public String toString()
	{
		return "Response [reply=" + reply + "]";
	}

	// protected TypeValue reply;
	protected Object reply;

	public Object getReply()
	{
		return reply;
	}

	@Override
	public MessageType getType()
	{
		return MessageType.Response;
	}

	@Override
	public void readExternal(SerializerInput in) throws IOException
	{
		if(in.readBoolean())
		{
			reply = in.readObject(Object.class);
		}
		
	}

	@Override
	public void writeExternal(SerializerOutput out) throws IOException
	{
		out.writeBoolean(null != reply);
		out.writeObject(reply, Object.class);
	}

}
