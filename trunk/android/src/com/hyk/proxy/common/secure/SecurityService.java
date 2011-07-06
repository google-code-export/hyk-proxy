/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Secure.java 
 *
 * @author yinqiwen [ 2010-5-15 | 10:28:48 PM]
 *
 */
package com.hyk.proxy.common.secure;

import java.nio.ByteBuffer;

/**
 *
 */
public interface SecurityService
{
	public String getName();
	
//	public InputStream getDecryptInputStream(InputStream input);
//	
//	public OutputStream getEncryptOutputStream(OutputStream output);
	
	public byte[] decrypt(byte[] value);
	public byte[] encrypt(byte[] value);
	public ByteBuffer decrypt(ByteBuffer value);
	public ByteBuffer encrypt(ByteBuffer value);
	public ByteBuffer[] decrypt(ByteBuffer[] value);
	public ByteBuffer[] encrypt(ByteBuffer[] value);
}
