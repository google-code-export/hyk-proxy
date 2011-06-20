/**
 * 
 */
package com.hyk.serializer;

import java.io.IOException;
import java.io.NotSerializableException;

import com.hyk.io.buffer.ChannelDataBuffer;

/**
 * @author Administrator
 *
 */
public interface Serializer {
	
	ChannelDataBuffer serialize(Object value) throws NotSerializableException, IOException ;
	ChannelDataBuffer serialize(Object value, ChannelDataBuffer data) throws NotSerializableException, IOException ;
	
	<T> T deserialize(Class<T> type,ChannelDataBuffer data) throws NotSerializableException, IOException,InstantiationException ;
}
