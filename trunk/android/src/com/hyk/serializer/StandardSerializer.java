package com.hyk.serializer;

import java.io.IOException;
import java.io.NotSerializableException;

import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.serializer.impl.OtherSerializerStream;
import com.hyk.serializer.impl.SerailizerStreamFactory;

public class StandardSerializer implements Serializer
{


	public ChannelDataBuffer serialize(Object obj) throws NotSerializableException, IOException
	{
		return serialize(obj, ChannelDataBuffer.allocate(256));
	}

	public ChannelDataBuffer serialize(Object obj, ChannelDataBuffer input) throws NotSerializableException, IOException
	{
		SerailizerStreamFactory.otherSerializer.marshal(obj, input);
		input.getOutputStream().close();
		return input;
	}

	public <T> T deserialize(Class<T> type, ChannelDataBuffer data) throws NotSerializableException, IOException, InstantiationException
	{
		OtherSerializerStream other = SerailizerStreamFactory.otherSerializer;
		Class clazz = type;
		T ret = (T)other.unmarshal(clazz, data);
		data.clear();
		return ret;
	}

}
