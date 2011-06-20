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
public class LongSerializerStream extends SerailizerStream<Long> {

	@Override
	protected Long unmarshal(Class<Long> type, ChannelDataBuffer data)
			throws NotSerializableException, IOException,
			InstantiationException {

		return readLong(data);
	}

	@Override
	protected ChannelDataBuffer marshal(Long value, ChannelDataBuffer data)
			throws NotSerializableException, IOException {
		writeLong(data, value);
		return data;
	}

}
