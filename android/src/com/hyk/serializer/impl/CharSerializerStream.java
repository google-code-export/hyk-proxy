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
public class CharSerializerStream extends SerailizerStream<Character> {

	@Override
	protected Character unmarshal(Class<Character> type, ChannelDataBuffer data)
			throws NotSerializableException, IOException,
			InstantiationException {
		return readChar(data);
	}

	@Override
	protected ChannelDataBuffer marshal(Character value, ChannelDataBuffer data)
			throws NotSerializableException, IOException {
		writeChar(data, value);
		return data;
	}

}
