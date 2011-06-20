/**
 * 
 */
package com.hyk.compress.compressor.none;

import java.io.IOException;

import com.hyk.compress.compressor.Compressor;
import com.hyk.io.buffer.ChannelDataBuffer;

/**
 * @author Administrator
 * 
 */
public class NoneCompressor implements Compressor
{
	public static final String NAME = "none";
	@Override
	public ChannelDataBuffer compress(ChannelDataBuffer data) throws IOException
	{
		return data;
	}

	@Override
	public ChannelDataBuffer decompress(ChannelDataBuffer data) throws IOException
	{
		return data;
	}
	
	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public ChannelDataBuffer compress(ChannelDataBuffer data, ChannelDataBuffer out) throws IOException
	{
		return out;
	}
}
