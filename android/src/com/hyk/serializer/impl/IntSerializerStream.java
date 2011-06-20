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
public class IntSerializerStream extends SerailizerStream<Integer>{

	@Override
	protected ChannelDataBuffer marshal(Integer obj,  ChannelDataBuffer data)
			throws NotSerializableException, IOException {
		writeInt(data, obj);
		return data;
	}

	protected Integer unmarshal(Class<Integer> type,ChannelDataBuffer data)
			throws NotSerializableException, IOException,
			InstantiationException {
		return readInt(data);
	}



}
