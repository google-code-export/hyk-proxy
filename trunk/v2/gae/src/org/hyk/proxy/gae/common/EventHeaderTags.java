/**
 * 
 */
package org.hyk.proxy.gae.common;

import java.io.IOException;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;

/**
 * @author qiyingwang
 *
 */
public class EventHeaderTags
{
	
	private static final short magicNumber = (short) 0xCAFE;
	public CompressorType compressor;
	public EncryptType encrypter;
	public String token;
	
	public static boolean readHeaderTags(Buffer buffer, EventHeaderTags tags)
	{
		if(buffer.readableBytes() < 8)
		{
			return false;
		}
		short magic = BufferHelper.readFixInt16(buffer, true);
		if(magic != magicNumber)
		{
			return false;
		}
		byte compressorType  = buffer.readByte();
		byte encyptType = buffer.readByte();
		buffer.skipBytes(4);
		tags.compressor = CompressorType.fromInt(compressorType);
		tags.encrypter = EncryptType.fromInt(encyptType);
		try
        {
	        tags.token = BufferHelper.readVarString(buffer);
        }
        catch (IOException e)
        {
	        return false;
        }
		return true;
	}
	
	public void encode(Buffer buffer)
	{
		BufferHelper.writeFixInt16(buffer, magicNumber, true);
		buffer.writeByte((byte) compressor.getValue());
		buffer.writeByte((byte) encrypter.getValue());
		BufferHelper.writeFixInt32(buffer, 0, true);
		BufferHelper.writeVarString(buffer, token);
	}
	
}
