/**
 * 
 */
package com.hyk.serializer.impl;

import java.io.IOException;
import java.io.NotSerializableException;

import com.hyk.io.buffer.ChannelDataBuffer;

/**
 * @author qiying.wang
 *
 */
public class EnumSerializerStream extends SerailizerStream<Enum> {


	@Override
	protected Enum unmarshal(Class<Enum> type, ChannelDataBuffer data)
			throws NotSerializableException, IOException,
			InstantiationException {
		String name = readString(data);
		return Enum.valueOf(type, name);
	}

	@Override
	protected ChannelDataBuffer marshal(Enum value,ChannelDataBuffer data)
			throws NotSerializableException, IOException {
		writeString(data, value.name());
		return data;
	}

}
