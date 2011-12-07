/**
 * 
 */
package org.arch.event;

import java.io.IOException;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;

/**
 * @author qiyingwang
 *
 */
@EventType(Event.RESERVED_SEGMENT_EVENT_TYPE)
@EventVersion(1)
public class EventSegment extends Event implements Comparable<EventSegment>
{
	public int sequence;
	public int total;
	public Buffer content;
	
	@Override
    public boolean onDecode(Buffer buffer)
    {
		try
        {
	        sequence = BufferHelper.readVarInt(buffer);
	        total = BufferHelper.readVarInt(buffer);
	        int len = BufferHelper.readVarInt(buffer);
	        byte[] b = new byte[len];
	        int k = buffer.read(b);
	        if(k != len)
	        {
	        	return false;
	        }
	        content = Buffer.wrapReadableContent(b);
        }
        catch (IOException e)
        {
	        return false;
        }
	    return false;
    }

	@Override
    public boolean onEncode(Buffer buffer)
    {
	    BufferHelper.writeVarInt(buffer, sequence);
	    BufferHelper.writeVarInt(buffer, total);
	    BufferHelper.writeVarInt(buffer, content.readableBytes());
	    buffer.write(content, content.readableBytes());
	    return true;
    }

	@Override
    public int compareTo(EventSegment o)
    {
	    if(sequence < o.sequence)
	    {
	    	return -1;
	    }
	    else if(sequence == o.sequence)
	    {
	    	return 0;
	    }
	    return 1;
    }

	
}
