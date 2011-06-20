/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: SerializerOutput.java 
 *
 * @author qiying.wang [ Jan 22, 2010 | 5:14:21 PM ]
 *
 */
package com.hyk.serializer;

import java.io.IOException;

/**
 *
 */
public interface SerializerOutput
{
	public void writeInt(int value) throws IOException;
	public void writeBoolean(boolean value) throws IOException;
	public void writeByte(byte value) throws IOException;
	public void writeChar(char value)throws IOException;;
	public void writeShort(short value)throws IOException;
	public void writeLong(long value)throws IOException;
	public void writeFloat(float value)throws IOException;
	public void writeDouble(double value)throws IOException;
	public void writeString(String value)throws IOException;
	public void writeBytes(byte[] value)throws IOException;
	public void writeBytes(byte[] value, int off, int len)throws IOException;
	public void writeRawBytes(byte[] value, int off, int len)throws IOException;
	//public void writeBytes(byte[] value)throws IOException;
	public void writeObject(Object value)throws IOException;
	public void writeObject(Object value, Class declType)throws IOException;
}
