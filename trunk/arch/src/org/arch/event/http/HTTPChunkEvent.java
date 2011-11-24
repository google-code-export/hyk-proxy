/**
 * 
 */
package org.arch.event.http;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;

/**
 * @author qiyingwang
 *
 */
@EventType(HTTPEventContants.HTTP_CHUNK_EVENT_TYPE)
@EventVersion(1)
public class HTTPChunkEvent extends Event
{
	public boolean isHttpsChunk;
	public byte[] content = new byte[0];
	
	@Override
    protected boolean onDecode(Buffer buffer)
    {
		isHttpsChunk = BufferHelper.readBool(buffer);
		try
        {
			int contenlen = BufferHelper.readVarInt(buffer);
			if (contenlen > 0)
			{
				content = new byte[contenlen];
				buffer.read(content);
			}
        }
        catch (Exception e)
        {
	        return false;
        }
	    return true;
    }
	@Override
    protected boolean onEncode(Buffer buffer)
    {
	    BufferHelper.writeBoolean(buffer, isHttpsChunk);
	    BufferHelper.writeVarInt(buffer, content.length);
	    buffer.write(content);
	    return true;
    }
}
