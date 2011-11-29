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
	public Buffer content = new Buffer(0);

	protected abstract boolean doDecode(Buffer buffer);

	protected abstract boolean doEncode(Buffer buffer);

	public int getCurrentContentLength()
	{
		return content.readableBytes();
	}
	
	public int getContentLength()
	{
		String lenheader = getHeader("Content-Length");
		if(null != lenheader)
		{
			return Integer.parseInt(lenheader.trim());
		}
		return 0;
	}
	
	public boolean containsHeader(String name)
	{
		return getHeaderPair(name) != null;
	}
	
	private KeyValuePair<String, String> getHeaderPair(String name)
	{
		for(KeyValuePair<String, String> header:headers)
		{
			if(header.getName().equalsIgnoreCase(name))
			{
				return header;
			}
		}
		return null;
	}
	
	public String getHeader(String name)
	{
		KeyValuePair<String, String> header = getHeaderPair(name);
		return null != header?header.getValue():null;
	}
	
	public List<KeyValuePair<String, String>> getHeaders()
	{
		return headers;
	}
	
	public void setHeader(String name, String value)
	{
		KeyValuePair<String, String> header = getHeaderPair(name);
		if(null != header)
		{
			header.setValue(value);
		}
		else
		{
			addHeader(name, value);
		}
	}
	
	public void addHeader(String name, String value)
	{
		KeyValuePair<String, String> header = new KeyValuePair<String, String>(name, value);
		headers.add(header);
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
				//content = new byte[contenlen];
				content.ensureWritableBytes(contenlen);
				content.write(buffer, contenlen);
				//buffer.read(content);
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
		BufferHelper.writeVarInt(buffer, content.readableBytes());
		int idx = content.getReadIndex();
		buffer.write(content, content.readableBytes());
		content.setReadIndex(idx);
		return false;
	}
}
