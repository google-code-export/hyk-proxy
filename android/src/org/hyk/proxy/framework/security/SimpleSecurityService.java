/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SimpleSecurityService.java 
 *
 * @author yinqiwen [ 2010-5-15 | 10:32:03 PM]
 *
 */
package org.hyk.proxy.framework.security;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 */
public class SimpleSecurityService
{
	private int	step;

	public SimpleSecurityService()
	{
		this(1);
	}

	public SimpleSecurityService(int step)
	{
		this.step = step;
	}


	public byte decrypt(byte value)
	{
		int v = value & 0xff;
		v += step;
		if(v >= 256)
		{
			v -= 256;
		}
		return (byte)v;
	}

	public byte[] decrypt(byte[] value)
	{
		for(int i = 0; i<value.length; i++)
		{
			value[i] = decrypt(value[i]);
		}
		return value;
	}

	public byte encrypt(byte value)
	{
		int k = value & 0xff;
		int v = k - step;
		if(v < 0)
		{
			v = 256 + v;
		}
		return (byte)v;
	}

	public byte[] encrypt(byte[] value)
	{
		for(int i = 0; i<value.length; i++)
		{
			value[i] = encrypt(value[i]);
		}
		return value;
	}

	public ByteBuffer decrypt(ByteBuffer value)
	{
		int pos = value.position();
		for(int i = 0; i<value.remaining(); i++)
		{
			value.put(pos + i, decrypt(value.get(pos + i)));
		}
		return value;
	}

	public ByteBuffer encrypt(ByteBuffer value)
	{
		int pos = value.position();
		for(int i = 0; i<value.remaining(); i++)
		{
			value.put(pos + i, encrypt(value.get(pos + i)));
		}
		return value;
	}

	public ByteBuffer[] decrypt(ByteBuffer[] value)
	{
		for(int i = 0; i<value.length; i++)
		{
			decrypt(value[i]);
		}
		return value;
	}

	public ByteBuffer[] encrypt(ByteBuffer[] value)
	{
		for(int i = 0; i<value.length; i++)
		{
			encrypt(value[i]);
		}
		return value;
	}
	
	public String encrypt(String str)
	{
		byte[] array = str.getBytes();
		array = encrypt(array);
		return new String(array);
	}


}
