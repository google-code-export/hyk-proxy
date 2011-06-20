/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: SerializerInput.java 
 *
 * @author qiying.wang [ Jan 22, 2010 | 5:14:00 PM ]
 *
 */
package com.hyk.serializer;

import java.io.IOException;

/**
 *
 */
public interface SerializerInput
{
	public int readInt() throws IOException;
	public boolean readBoolean()throws IOException;
	public short readShort()throws IOException;
	public long readLong()throws IOException;
	public float readFloat()throws IOException;
	public double readDouble()throws IOException;
	public char readChar()throws IOException;
	public byte readByte()throws IOException;
	public String readString()throws IOException;
	public byte[] readBytes() throws IOException;
	public void readBytes(byte[] content, int off, int len) throws IOException;
	public void readBytes(byte[] content) throws IOException;
	//public byte[] readBytes()throws IOException;
	public <T> T readObject(Class<T> type) throws IOException;
	//public Object readProxyObject() throws IOException;
}
