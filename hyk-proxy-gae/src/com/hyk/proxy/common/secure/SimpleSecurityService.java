/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SimpleSecurityService.java 
 *
 * @author yinqiwen [ 2010-5-15 | 10:32:03 PM]
 *
 */
package com.hyk.proxy.common.secure;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 */
public class SimpleSecurityService implements SecurityService
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

	@Override
	public String getName()
	{
		return "se" + step;
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

	@Override
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

	@Override
	public byte[] encrypt(byte[] value)
	{
		for(int i = 0; i<value.length; i++)
		{
			value[i] = encrypt(value[i]);
		}
		return value;
	}

	@Override
	public ByteBuffer decrypt(ByteBuffer value)
	{
		int pos = value.position();
		for(int i = 0; i<value.remaining(); i++)
		{
			value.put(pos + i, decrypt(value.get(pos + i)));
		}
		return value;
	}

	@Override
	public ByteBuffer encrypt(ByteBuffer value)
	{
		int pos = value.position();
		for(int i = 0; i<value.remaining(); i++)
		{
			value.put(pos + i, encrypt(value.get(pos + i)));
		}
		return value;
	}

	@Override
	public ByteBuffer[] decrypt(ByteBuffer[] value)
	{
		for(int i = 0; i<value.length; i++)
		{
			decrypt(value[i]);
		}
		return value;
	}

	@Override
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
	
//	public static void main(String[] args)
//	{
//		SimpleSecurityService service = new SimpleSecurityService();
//		
//		byte[] test = "REMOTE_SERVICE_MANAGER".getBytes();
//		System.out.println(Arrays.toString(test));
//		ByteBuffer buf = ByteBuffer.allocate(4+test.length);
//		buf.putInt(1);
//		buf.put(service.encrypt(test));
//		buf.flip();
//		System.out.println(Arrays.toString(test));
//		buf.getInt();
//		service.decrypt(buf);
//		System.arraycopy(buf.array(), 4, test, 0, test.length);
//		System.out.println(Arrays.toString(test));
//		//System.out.println(Arrays.toString(service.encrypt(test)));
//		//System.out.println(Arrays.toString(service.decrypt(test)));
//	}

}
