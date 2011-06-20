/**
 * This file is part of the hyk-util project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: DataBuffer.java 
 *
 * @author qiying.wang [ May 24, 2010 | 2:22:18 PM ]
 *
 */
package com.hyk.io.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ChannelDataBuffer
{
	// private static final int DEFAULT_BUFFER_SIZE = 256;
	private int					readerIndex;
	private int					writerIndex;
	private int					markedReaderIndex;
	private int					markedWriterIndex;

	private List<ByteArray>		bufferList	= new ArrayList<ByteArray>();

	public final OutputStream	out			= new OutputStream();
	public final InputStream	in			= new InputStream();

	private ChannelDataBuffer(byte[] buffer, int off, int len)
	{
		bufferList.add(new ByteArray(buffer, off, len));
	}

	public static ChannelDataBuffer wrap(byte[] buffer)
	{
		return new ChannelDataBuffer(buffer, 0, buffer.length);
	}

	public static ChannelDataBuffer wrap(byte[] buffer, int off, int len)
	{
		return new ChannelDataBuffer(buffer, off, len);
	}
	
	public static ChannelDataBuffer wrap(ChannelDataBuffer... buffers)
	{
		ChannelDataBuffer base = buffers[0];
		for(int i = 1; i < buffers.length; i++)
		{
			base.bufferList.addAll(buffers[i].bufferList);
		}
		return base;
	}

	public static ChannelDataBuffer allocate(int size)
	{
		return wrap(new byte[size]);
	}

	public java.io.OutputStream getOutputStream()
	{
		return out;
	}

	public java.io.InputStream getInputStream()
	{
		return in;
	}

	class OutputStream extends java.io.OutputStream
	{
		@Override
		public void write(int b) throws IOException
		{
			writeByte((byte)b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException
		{
			writeBytes(b, off, len);
		}

		@Override
		public void close() throws IOException
		{
			flip();
		}
	}

	class InputStream extends java.io.InputStream
	{

		@Override
		public int available() throws IOException
		{
			return readableBytes();
		}

		@Override
		public int read() throws IOException
		{
			if(!readable()) return -1;
			return readByte() & 0xff;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException
		{
			if(!readable()) return -1;
			len = Math.min(len, readableBytes());
			readBytes(b, off, len);
			return len;
		}
	}

	public int readerIndex()
	{
		return readerIndex;
	}

	public void readerIndex(int readerIndex)
	{
		if(readerIndex < 0 || readerIndex > writerIndex)
		{
			throw new IndexOutOfBoundsException();
		}
		this.readerIndex = readerIndex;
	}

	public int writerIndex()
	{
		return writerIndex;
	}

	public void writerIndex(int writerIndex)
	{
		if(writerIndex < readerIndex || writerIndex > capacity())
		{
			throw new IndexOutOfBoundsException();
		}
		this.writerIndex = writerIndex;
	}

	public void setIndex(int readerIndex, int writerIndex)
	{
		if(readerIndex < 0 || readerIndex > writerIndex || writerIndex > capacity())
		{
			throw new IndexOutOfBoundsException();
		}
		this.readerIndex = readerIndex;
		this.writerIndex = writerIndex;
	}

	public void clear()
	{
		readerIndex = writerIndex = 0;
	}

	public boolean readable()
	{
		return readableBytes() > 0;
	}

	public boolean writable()
	{
		return writableBytes() > 0;
	}

	public int readableBytes()
	{
		return capacity() - readerIndex;
	}

	public int writableBytes()
	{
		return capacity() - writerIndex;
	}

	public void flip()
	{
		ByteArray buffer = getWriteBuffer(0);
		int offset = getWriteBufferOffset();
		buffer.length = offset - buffer.off;
	}

	public int capacity()
	{
		int capacity = 0;
		for(ByteArray buffer : bufferList)
		{
			capacity += buffer.capacity();
		}
		return capacity;
	}

	public void markReaderIndex()
	{
		markedReaderIndex = readerIndex;
	}

	public void resetReaderIndex()
	{
		readerIndex(markedReaderIndex);
	}

	public void markWriterIndex()
	{
		markedWriterIndex = writerIndex;
	}

	public void resetWriterIndex()
	{
		writerIndex = markedWriterIndex;
	}

	protected ByteArray allocateNewWriteBuffer(int expectedRemaining, int latestBufSize)
	{
		int allocateSize = Math.max(latestBufSize << 1, expectedRemaining);
		ByteArray buf = new ByteArray(allocateSize);
		bufferList.add(buf);
		return buf;
	}

	private ByteArray getWriteBuffer(int expectedRemaining)
	{
		int size = 0;
		int latestBufSize = 0;
		for(ByteArray buffer : bufferList)
		{
			latestBufSize = buffer.capacity();
			size += latestBufSize;
			if(size > (writerIndex))
			{
				return buffer;
			}
		}
		return allocateNewWriteBuffer(expectedRemaining, latestBufSize);
	}

	private ByteArray getReadBuffer()
	{
		int size = 0;
		int latestBufSize = 0;
		for(ByteArray buffer : bufferList)
		{
			latestBufSize = buffer.capacity();
			size += latestBufSize;
			if(size > readerIndex)
			{
				return buffer;
			}
		}
		throw new IndexOutOfBoundsException();
	}

	private int getWriteBufferOffset()
	{
		int size = 0;
		for(ByteArray buffer : bufferList)
		{
			size += buffer.capacity();
			if(size > (writerIndex))
			{
				int remain = (size - writerIndex);
				return buffer.capacity() - remain;
			}
		}
		return 0;
	}

	private int getReadBufferOffset()
	{
		int size = 0;
		for(ByteArray buffer : bufferList)
		{
			size += buffer.capacity();
			if(size > (readerIndex))
			{
				int remain = (size - readerIndex);
				return buffer.capacity() - remain;
			}
		}
		return 0;
	}

	public void writeBytes(byte[] src, int srcIndex, int length)
	{
		int writeTotalLen = 0;
		while(writeTotalLen < length)
		{
			ByteArray buffer = getWriteBuffer(length - writeTotalLen);
			int bufoff = getWriteBufferOffset();
			int remaining = buffer.capacity() - bufoff;
			int writeLen = remaining < (length - writeTotalLen) ? remaining : (length - writeTotalLen);
			System.arraycopy(src, srcIndex, buffer.array, bufoff + buffer.off, writeLen);
			// buffer.setBytes(src, srcIndex, writeLen);
			writeTotalLen += writeLen;
			srcIndex += writeLen;
			writerIndex += writeLen;
		}
	}

	public void writeBytes(ByteBuffer bytes)
	{
		int writeTotalLen = 0;
		int length = bytes.remaining();
		while(writeTotalLen < length)
		{
			ByteArray buffer = getWriteBuffer(length - writeTotalLen);
			int bufoff = getWriteBufferOffset();
			int remaining = buffer.capacity() - bufoff;
			int writeLen = remaining < (length - writeTotalLen) ? remaining : (length - writeTotalLen);
			bytes.get(buffer.array, buffer.off + bufoff, writeLen);
			// buffer.setBytes(src, srcIndex, writeLen);
			writeTotalLen += writeLen;
			writerIndex += writeLen;
		}
	}
	
	

	public void writeByte(byte value)
	{
		getWriteBuffer(1).set(getWriteBufferOffset(), value);
		writerIndex++;
	}

	public void writeBytes(byte[] src)
	{
		writeBytes(src, 0, src.length);
	}

	// public void writeBytes(DataBuffer src)
	// {
	// int writeTotalLen = 0;
	// int length = src.writableBytes();
	// while(writeTotalLen < length)
	// {
	// ByteArrayWrapper writeBuf = src.getReadBuffer();
	// int writeOff = src.getReadBufferOffset();
	// writeBytes(writeBuf.array, writeOff + writeBuf.off, writeBuf.length -
	// writeOff);
	// src.writerIndex += (writeBuf.length - writeOff);
	// writeTotalLen += (writeBuf.length - writeOff);
	// }
	// }

	// public void writeBytes(ByteBuffer src)
	// {
	// int length = src.remaining();
	// int writeTotalLen = 0;
	// while(writeTotalLen < length)
	// {
	// ByteArrayWrapper buffer = getWriteBuffer(length - writeTotalLen);
	// int bufoff = getWriteBufferOffset();
	// int remaining = buffer.capacity() - bufoff;
	// int writeLen = remaining < (length - writeTotalLen) ? remaining : (length
	// - writeTotalLen);
	// src.get(buffer.array, bufoff + buffer.off, writeLen);
	// //buffer.setByteBuffer(src, writeLen);
	// writeTotalLen += writeLen;
	// }
	// writerIndex += length;
	// }

	public byte readByte()
	{
		byte ret = getReadBuffer().get(getReadBufferOffset());
		readerIndex++;
		return ret;
	}

	public void readBytes(byte[] dst)
	{
		readBytes(dst, 0, dst.length);
	}

	public void readBytes(byte[] dst, int dstIndex, int length)
	{
		checkReadableBytes(length);
		int readTotalLen = 0;
		while(readTotalLen < length)
		{
			ByteArray buffer = getReadBuffer();
			int bufoff = getReadBufferOffset();
			int remaining = buffer.capacity() - bufoff;
			int writeLen = remaining < (length - readTotalLen) ? remaining : (length - readTotalLen);
			System.arraycopy(buffer.array, buffer.off + bufoff, dst, dstIndex, writeLen);
			readTotalLen += writeLen;
			dstIndex += writeLen;
			readerIndex += writeLen;
		}

	}

	public ByteBuffer readByteBuffer(int length)
	{
		checkReadableBytes(length);

		ByteArray buffer = getReadBuffer();
		int bufoff = getReadBufferOffset();
		int remaining = buffer.capacity() - bufoff;
		if(remaining >= length)
		{
			// avoid array copy
			return ByteBuffer.wrap(buffer.array, buffer.off + bufoff, length);
		}
		int readTotalLen = 0;
		ByteBuffer ret = ByteBuffer.allocate(length);
		while(readTotalLen < length)
		{
			int writeLen = remaining < (length - readTotalLen) ? remaining : (length - readTotalLen);
			// System.arraycopy(buffer, bufoff, dst, dstIndex, writeLen);
			ret.put(buffer.array, buffer.off + bufoff, writeLen);
			readTotalLen += writeLen;
			buffer = getReadBuffer();
			bufoff = getReadBufferOffset();
			remaining = buffer.capacity() - bufoff;
			readerIndex += writeLen;
		}

		ret.flip();
		return ret;
	}

	// public void readBytes(ByteBuffer dst)
	// {
	// int length = dst.remaining();
	// dst.mark();
	// checkReadableBytes(dst.remaining());
	// int readTotalLen = 0;
	// while(readTotalLen < length)
	// {
	// ByteArrayWrapper buffer = getReadBuffer();
	// int bufoff = getReadBufferOffset();
	// int remaining = buffer.capacity() - bufoff;
	// int writeLen = remaining < (length - readTotalLen) ? remaining : (length
	// - readTotalLen);
	// if(length == writeLen)
	// {
	//				
	// }
	// //System.arraycopy(buffer, bufoff, dst, dstIndex, writeLen);
	// readTotalLen += writeLen;
	// //dstIndex += writeLen;
	// }
	// dst.reset();
	// }

	// public void readBytes(ByteArray dst)
	// {
	// int readTotalLen = 0;
	// int length = readableBytes();
	// while(readTotalLen < length)
	// {
	// Buffer writeBuf = getReadBuffer();
	// int writeOff = getReadBufferOffset();
	// dst.writeBytes(writeBuf.array, writeOff + writeBuf.off, writeBuf.length -
	// writeOff);
	// readerIndex += (writeBuf.length - writeOff);
	// readTotalLen += (writeBuf.length - writeOff);
	// }
	// }

	protected void checkReadableBytes(int minimumReadableBytes)
	{
		if(readableBytes() < minimumReadableBytes)
		{
			throw new IndexOutOfBoundsException();
		}
	}

	public static byte[] asByteArray(ChannelDataBuffer buffer)
	{
		if(buffer.bufferList.size() == 1)
		{
			ByteArray array = buffer.bufferList.get(0);
			if(array.length == array.array.length && array.off == 0)
			{
				return array.array;
			}
		}
		byte[] ret = new byte[buffer.capacity()];
		buffer.readBytes(ret);
		return ret;
	}

	public static ByteBuffer asByteBuffer(ChannelDataBuffer buffer)
	{
		if(buffer.bufferList.size() == 1)
		{
			ByteArray array = buffer.bufferList.get(0);
			return ByteBuffer.wrap(array.array, array.off, array.length);
		}
		ByteBuffer ret = ByteBuffer.allocate(buffer.capacity());
		for(ByteArray array : buffer.bufferList)
		{
			ret.put(array.array, array.off, array.length);
		}
		ret.flip();
		return ret;
	}

	public static ByteBuffer[] asByteBuffers(ChannelDataBuffer buffer)
	{
		ByteBuffer[] ret = new ByteBuffer[buffer.bufferList.size()];
		for(int i = 0; i < ret.length; i++)
		{
			ByteArray array = buffer.bufferList.get(i);
			ret[i] = ByteBuffer.wrap(array.array, array.off, array.length);
		}
		return ret;
	}
	

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + '(' + "ridx=" + readerIndex + ", " + "widx=" + writerIndex + ", " + "cap=" + capacity() + ')';
	}

}
