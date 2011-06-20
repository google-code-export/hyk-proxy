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
public class DoubleSerializerStream extends SerailizerStream<Double> {

	@Override
	protected Double unmarshal(Class<Double> type, ChannelDataBuffer data)
			throws NotSerializableException, IOException,
			InstantiationException {
		return readDouble(data);
	}

	@Override
	protected ChannelDataBuffer marshal(Double value, ChannelDataBuffer data)
			throws NotSerializableException, IOException {
		writeDouble(data, value);
		return data;
	}

}
