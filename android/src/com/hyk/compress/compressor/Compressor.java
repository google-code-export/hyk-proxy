/**
 * 
 */
package com.hyk.compress.compressor;

import java.io.IOException;

import com.hyk.io.buffer.ChannelDataBuffer;

/**
 * @author Administrator
 *
 */
public interface Compressor {

	public String getName();
	public ChannelDataBuffer compress(ChannelDataBuffer data) throws IOException;
	public ChannelDataBuffer compress(ChannelDataBuffer data, ChannelDataBuffer out) throws IOException;
	public ChannelDataBuffer decompress(ChannelDataBuffer data) throws IOException;
}
