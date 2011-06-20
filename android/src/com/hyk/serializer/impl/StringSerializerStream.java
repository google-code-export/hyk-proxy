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
public class StringSerializerStream extends SerailizerStream<String> {

	@Override
	protected String unmarshal(Class<String> type, ChannelDataBuffer data)
			throws NotSerializableException, IOException,
			InstantiationException {
		return readString(data);
	}

	@Override
	protected ChannelDataBuffer marshal(String value, ChannelDataBuffer data)
			throws NotSerializableException, IOException {
		writeString(data, value);
		return data;
	}

}
