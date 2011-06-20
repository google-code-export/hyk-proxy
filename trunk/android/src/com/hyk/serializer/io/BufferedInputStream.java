/**
 * 
 */
package com.hyk.serializer.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author qiying.wang
 *
 */
public class BufferedInputStream extends InputStream {
	
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	
	private byte[] buffer;
	private int pos;
	private int limit;
	
	private InputStream is;
	
	public BufferedInputStream(InputStream is) throws IOException
	{
		this(is, DEFAULT_BUFFER_SIZE);
	}
	
	public BufferedInputStream(byte[] buffer) throws IOException
	{
		this(buffer, 0, buffer.length);
	}
	
	public BufferedInputStream(byte[] buffer, int off, int len) throws IOException
	{
		this.buffer = buffer;;
		pos = off;
		limit = off + len;
		this.is = new InputStream() {
			public int read() throws IOException {
				return -1;
			}
		};
	}
	
	public BufferedInputStream(InputStream is, int bufferSize) throws IOException
	{
		this.is = is;
		if(bufferSize <= 0)
		{
			bufferSize = DEFAULT_BUFFER_SIZE;
		}
		buffer = new byte[bufferSize];
		pos = bufferSize;
		limit = bufferSize;
		refreshBuffer();
	}
	
	public int available() throws IOException {
		return limit - pos + is.available();
	}
	
	private boolean refreshBuffer() throws IOException
	{
		if(pos < limit)
		{
			return true;
		}
     
        int len = is.read(buffer);
        if(len > 0)
        {
        	pos = 0;
        	limit = len;
        	return true;
        }
        throw new IOException("No data in stream!");
	}
	
	@Override
	public int read() throws IOException {
		if (pos == limit) {
			refreshBuffer();
		}
		return buffer[pos++];
	}
	
	public int read(byte b[], int off, int len) throws IOException
	{
		if(limit - pos >= len)
		{
			System.arraycopy(buffer, pos, b, off, len);
			pos += len;
			return len;
		}
		else
		{
			System.arraycopy(buffer, pos, b, off, limit-pos);
			pos = limit;
			int readed = limit-pos;
			refreshBuffer();
			return readed + read(b, off+readed, len-readed);
		}
	}
	
	public String readString(int size)throws IOException 
	{
		if(size > 0 && limit - pos >= size)
		{
			String s = new String(buffer, pos, size, "UTF-8");
			pos += size;
			return s;
		}
		else
		{
			byte[] data = new byte[size];
			int len = read(data);
			if(len != size)
			{
				throw new IOException("No data in stream!");
			}
			return new String(data, "UTF-8");
		}
		
	}
	
	public void close() throws IOException 
	{
		is.close();
	}

}
