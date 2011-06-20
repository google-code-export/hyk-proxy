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
public class FloatSerializerStream extends SerailizerStream<Float> {

	@Override
	protected Float unmarshal(Class<Float> type, ChannelDataBuffer data)
			throws NotSerializableException, IOException,
			InstantiationException {
		return readFloat(data);
	}

	//@Override
	protected ChannelDataBuffer marshal(Float value,ChannelDataBuffer data)
			throws NotSerializableException, IOException {
		writeFloat(data, value);
		return data;
	}

}
