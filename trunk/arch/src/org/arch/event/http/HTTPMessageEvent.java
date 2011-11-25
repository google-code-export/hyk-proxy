/**
 * 
 */
package org.arch.event.http;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.common.KeyValuePair;
import org.arch.event.Event;

/**
 * @author qiyingwang
 * 
 */
public abstract class HTTPMessageEvent extends Event
{
	public List<KeyValuePair<String, String>> headers = new LinkedList<KeyValuePair<String, String>>();
	public byte[] content = new byte[0];

	protected abstract boolean doDecode(Buffer buffer);

	protected abstract boolean doEncode(Buffer buffer);

	
	public String getHeader(String name)
	{
		for(KeyValuePair<String, String> header:headers)
		{
			if(header.getName().equalsIgnoreCase(name))
			{
				return header.getValue();
			}
		}
		return null;
	}
	
	@Override
	protected boolean onDecode(Buffer buffer)
	{
		boolean ret = doDecode(buffer);
		if (!ret)
		{
			return false;
		}
		try
		{
			int headernum = BufferHelper.readVarInt(buffer);
			for (int i = 0; i < headernum; i++)
			{
				String name = BufferHelper.readVarString(buffer);
				String value = BufferHelper.readVarString(buffer);
				KeyValuePair<String, String> kv = new KeyValuePair<String, String>(
				        name, value);
				headers.add(kv);
			}
			int contenlen = BufferHelper.readVarInt(buffer);
			if (contenlen > 0)
			{
				content = new byte[contenlen];
				buffer.read(content);
			}
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}

	@Override
	protected boolean onEncode(Buffer buffer)
	{
		boolean ret = doEncode(buffer);
		if (!ret)
		{
			return false;
		}
		BufferHelper.writeVarInt(buffer, headers.size());
		for (KeyValuePair<String, String> kv : headers)
		{
			BufferHelper.writeVarString(buffer, kv.getName());
			BufferHelper.writeVarString(buffer, kv.getValue());
		}
		BufferHelper.writeVarInt(buffer, content.length);
		buffer.write(content);
		return false;
	}
}
