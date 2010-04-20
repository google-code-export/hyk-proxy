package com.hyk.proxy.gae.common.extension.compress.lzf;

import java.io.IOException;

import com.hyk.compress.compressor.Compressor;

import com.hyk.util.buffer.ByteArray;

public class LZFCompressor implements Compressor
{
	@Override
	public ByteArray compress(ByteArray data) throws IOException
	{
		ByteArray ret = ByteArray.allocate(data.size() / 3);
		LZFOutputStream lzfos = new LZFOutputStream(ret.output);
		byte[] raw = data.rawbuffer();
		int offset = data.position();
		int len = data.size();
		lzfos.write(raw, offset, len);
		lzfos.close();
		return ret;
	}

	@Override
	public ByteArray decompress(ByteArray data) throws IOException
	{
		ByteArray ret = ByteArray.allocate(data.size() * 3);
		LZFInputStream lzfis = new LZFInputStream(data.input);
		int b;
		while((b = lzfis.read()) != -1)
		{
			ret.output.write(b);
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

}
