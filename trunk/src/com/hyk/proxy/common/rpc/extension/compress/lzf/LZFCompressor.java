package com.hyk.proxy.common.rpc.extension.compress.lzf;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.hyk.compress.compressor.Compressor;
import com.hyk.io.ByteDataBuffer;

public class LZFCompressor implements Compressor
{

	@Override
	public ByteDataBuffer compress(ByteDataBuffer data) throws IOException
	{
		ByteDataBuffer ret = ByteDataBuffer.allocate(data.size() / 3);
		return compress(data, ret);
	}

	@Override
	public ByteDataBuffer decompress(ByteDataBuffer data) throws IOException
	{
		ByteDataBuffer ret = ByteDataBuffer.allocate(data.size() * 3);
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
	public ByteDataBuffer compress(ByteDataBuffer data, ByteDataBuffer out) throws IOException
	{
		ByteDataBuffer ret = out;
		LZFOutputStream lzfos = new LZFOutputStream(ret.getOutputStream());
		//List<ByteBuffer> bufs = data.buffers();
		for(ByteBuffer buf : data.buffers())
		{
			byte[] raw = buf.array();
			int offset = buf.position();
			int len = buf.remaining();
			lzfos.write(raw, offset, len);
		}

		lzfos.close();
		return ret;
	}
}
