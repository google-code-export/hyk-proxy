/**
 * This file is part of the hyk-util project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SimpleByteArray.java 
 *
 * @author qiying.wang [ May 18, 2010 | 2:57:28 PM ]
 *
 */
package com.hyk.io.buffer;

/**
 *
 */
class ByteArray 
{
	public ByteArray(int size)
	{
		this.array = new byte[size];
		this.off = 0;
		this.length = size;
	}

	public ByteArray(byte[] array)
	{
		this.array = array;
		this.off = 0;
		this.length = array.length;
	}

	public ByteArray(byte[] array, int off, int len)
	{
		this.array = array;
		this.off = off;
		this.length = len;
	}


	public byte get(int index)
	{
		return array[off + index];
	}


	public void set(int index, byte b)
	{
		array[off + index] = b;
	}

	byte[]		array;
	int			off;
	int	length;


	public int capacity()
	{
		return length;
	}
}
