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
public class ByteSerializerStream extends SerailizerStream<Byte> {

	@Override
	protected Byte unmarshal(Class<Byte> type, ChannelDataBuffer data)
			throws NotSerializableException, IOException,
			InstantiationException {
		return readByte(data);
	}

	@Override
	protected ChannelDataBuffer marshal(Byte value,ChannelDataBuffer data)
			throws NotSerializableException, IOException {
		writeByte(data, value);
		return data;
	}

}
