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
public class ShortSerializerStream extends SerailizerStream<Short> {


	@Override
	protected Short unmarshal(Class<Short> type, ChannelDataBuffer data)
			throws NotSerializableException, IOException,
			InstantiationException {
		return readShort(data);
	}

	@Override
	protected ChannelDataBuffer marshal(Short value, ChannelDataBuffer data)
			throws NotSerializableException, IOException {
		writeInt(data, value);
		return data;
	}

}
