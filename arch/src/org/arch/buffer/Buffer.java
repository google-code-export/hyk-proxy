/**
 * 
 */
package org.arch.buffer;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author qiyingwang
 * 
 */
public class Buffer
{

	private static final int BUFFER_MAX_READ = 8192;
	private static final int DEFAULT_BUFFER_SIZE = 32;

	private byte[] buffer = new byte[0];

	private int read_index;
	private int write_index;

	// private int buffer_len;

	public static Buffer wrapReadableContent(byte[] content)
	{
		Buffer buf = new Buffer(content);
		buf.write_index = content.length;
		buf.read_index = 0;
		return buf;
	}
	
	private Buffer(byte[] content)
	{
		this.buffer = content;
	}
	
	public Buffer()
	{
		this(32);
	}

	public Buffer(int capactity)
	{
		ensureWritableBytes(capactity);
	}

	public int getReadIndex()
	{
		return read_index;
	}

	public int getWriteIndex()
	{
		return write_index;
	}

	public void setReadIndex(int idx)
	{
		read_index = idx;
	}

	public void advanceReadIndex(int step)
	{
		read_index += step;
	}

	public void setWriteIndex(int idx)
	{
		write_index = idx;
	}

	public void advanceWriteIndex(int step)
	{
		write_index += step;
	}

	public boolean readable()
	{
		return write_index > read_index;
	}

	public boolean writeable()
	{
		return buffer.length > write_index;
	}

	public int readableBytes()
	{
		return readable() ? write_index - read_index : 0;
	}

	public int writeableBytes()
	{
		return writeable() ? buffer.length - write_index : 0;
	}

	public int compact(int leastLength)
	{
		int writableBytes = writeableBytes();
		if (writableBytes < leastLength)
		{
			return 0;
		}
		int readableBytes = readableBytes();
		int total = capacity();
		byte[] newSpace = null;
		if (readableBytes > 0)
		{
			newSpace = new byte[readableBytes];
			System.arraycopy(buffer, read_index, newSpace, 0, readableBytes);
		}
		read_index = 0;
		write_index = readableBytes;
		buffer = newSpace;
		return total - readableBytes;
	}

	public boolean ensureWritableBytes(int minWritableBytes)
	{
		if (writeableBytes() >= minWritableBytes)
		{
			return true;
		}
		else
		{
			int newCapacity = capacity();
			if (0 == newCapacity)
			{
				newCapacity = DEFAULT_BUFFER_SIZE;
			}
			int minNewCapacity = getWriteIndex() + minWritableBytes;
			while (newCapacity < minNewCapacity)
			{
				newCapacity <<= 1;
			}
			byte[] tmp = new byte[newCapacity];
			System.arraycopy(buffer, 0, tmp, 0, readableBytes());
			buffer = tmp;
			return true;
		}
	}

	public boolean reserve(int len)
	{
		return ensureWritableBytes(len);
	}

	public byte[] getRawBuffer()
	{
		return buffer;
	}

	public int capacity()
	{
		return buffer.length;
	}

	public void clear()
	{
		write_index = read_index = 0;
	}

	public int read(byte[] data_out, int off, int datlen)
	{
		if (datlen > readableBytes())
		{
			return -1;
		}
		System.arraycopy(buffer, read_index, data_out, off, datlen);
		read_index += datlen;
		return datlen;
	}
	
	public int read(byte[] data_out)
	{
		return read(data_out, 0, data_out.length);
	}

	public int write(byte[] data_in)
	{
		return write(data_in, 0, data_in.length);
	}
	
	public int write(byte[] data_in, int off, int datlen)
	{
		if (!ensureWritableBytes(datlen))
		{
			return -1;
		}
		System.arraycopy(data_in, off, buffer, write_index, datlen);
		write_index += datlen;
		return datlen;
	}

	public int write(Buffer unit, int datlen)
	{

		if (datlen > unit.readableBytes())
		{
			datlen = unit.readableBytes();
		}
		int ret = write(unit.buffer, unit.read_index, datlen);
		if (ret > 0)
		{
			unit.read_index += ret;
		}
		return ret;
	}

	public int writeByte(byte ch)
	{
		if (!ensureWritableBytes(1))
		{
			return -1;
		}
		buffer[write_index++] = ch;
		return 1;
	}

	public int read(Buffer unit, int datlen)
	{
		return unit.write(this, datlen);
	}

	public byte readByte()
	{
		if(!readable())
		{
			throw new IndexOutOfBoundsException();
		}
		return  buffer[read_index++];
	}

	public void skipBytes(int len)
	{
		advanceReadIndex(len);
	}

	public void discardReadedBytes()
	{
		if (read_index > 0)
		{
			if (readable())
			{
				int tmp = readableBytes();
				System.arraycopy(buffer, read_index, buffer, 0, tmp);
				read_index = 0;
				write_index = tmp;
			}
			else
			{
				read_index = write_index = 0;
			}
		}
	}
	
	public int read(InputStream is)
	{
		int writeCurPos = getWriteIndex();
		while(true)
		{
			try
            {
	            int len = is.read(buffer, getWriteIndex(), writeableBytes());
	            if(len <= 0)
	            {
	            	break;
	            }
	            write_index += len;
            }
            catch (IOException e)
            {
            	
	            break;
            }
		}
		return getWriteIndex() - writeCurPos;
	}

}
