/**
 * 
 */
package org.arch.event;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;

/**
 * @author qiyingwang
 *
 */
public class EventHeader
{
	public int type;
	public int version;
	public int hash;
	
	public EventHeader(){}
	
	public EventHeader(TypeVersion tv, int hash)
	{
		type = tv.type;
		version = tv.version;
		this.hash = hash;
	}
	
	public boolean encode(Buffer buffer)
	{
		BufferHelper.writeVarInt(buffer, type);
		BufferHelper.writeVarInt(buffer, version);
		BufferHelper.writeVarInt(buffer, hash);
		return true;
	}
	
	public boolean decode(Buffer buffer)
	{
		try
        {
			type = BufferHelper.readVarInt(buffer);
			version = BufferHelper.readVarInt(buffer);
			hash = BufferHelper.readVarInt(buffer);
        }
        catch (Exception e)
        {
	        return false;
        }
		return true;
	}
	
	@Override
	public String toString()
	{
	    return "" + type + ":" + version + ":" + hash;
	}
}
