package com.hyk.proxy.common.rpc.extension.compress.lzf;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.hyk.compress.compressor.Compressor;
import com.hyk.io.buffer.ChannelDataBuffer;

public class LZFCompressor implements Compressor
{

	@Override
	public ChannelDataBuffer compress(ChannelDataBuffer data) throws IOException
	{
		ChannelDataBuffer ret = ChannelDataBuffer.allocate(data.readableBytes() / 3);

		return compress(data, ret);
	}

	@Override
	public ChannelDataBuffer decompress(ChannelDataBuffer data) throws IOException
	{
		ChannelDataBuffer ret = ChannelDataBuffer.allocate(data.readableBytes() * 3);
		LZFInputStream lzfis = new LZFInputStream(data.getInputStream());
		int b;
		while((b = lzfis.read()) != -1)
		{
			ret.getOutputStream().write(b);
		}
		lzfis.close();
		ret.flip();
		return ret;
	}

	@Override
	public String getName()
	{
		return "lzf";
	}

	@Override
	public ChannelDataBuffer compress(ChannelDataBuffer data, ChannelDataBuffer out) throws IOException
	{
		LZFOutputStream lzfos = new LZFOutputStream(out.getOutputStream());
		ByteBuffer[] bufs = ChannelDataBuffer.asByteBuffers(data);
		for(ByteBuffer buf : bufs)
		{
			byte[] raw = buf.array();
			int offset = buf.position();
			int len = buf.remaining();
			lzfos.write(raw, offset, len);
		}

		lzfos.close();
		return out;
	}

}
