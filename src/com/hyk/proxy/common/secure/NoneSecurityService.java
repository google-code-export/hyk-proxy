/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: NoneSecurityService.java 
 *
 * @author yinqiwen [ 2010-5-15 | 11:07:57 PM ]
 *
 */
package com.hyk.proxy.common.secure;

import java.nio.ByteBuffer;

/**
 *
 */
public class NoneSecurityService implements SecurityService
{

//	@Override
//	public InputStream getDecryptInputStream(InputStream input)
//	{
//		return input;
//	}
//
//	@Override
//	public OutputStream getEncryptOutputStream(OutputStream output)
//	{
//		return output;
//	}

	@Override
	public String getName()
	{
		return NAME;
	}
	
	public static final String NAME = "none";


	@Override
	public byte[] decrypt(byte[] value)
	{
		return value;
	}


	@Override
	public byte[] encrypt(byte[] value)
	{
		return value;
	}

	@Override
	public ByteBuffer decrypt(ByteBuffer value)
	{
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public ByteBuffer encrypt(ByteBuffer value)
	{
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public ByteBuffer[] decrypt(ByteBuffer[] value)
	{
		return value;
	}

	@Override
	public ByteBuffer[] encrypt(ByteBuffer[] value)
	{
		return value;
	}

}
