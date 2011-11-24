/**
 * 
 */
package org.arch.event;

import org.arch.buffer.Buffer;

/**
 * @author qiyingwang
 *
 */
@EventType(Event.RESERVED_SEGMENT_EVENT_TYPE)
@EventVersion(1)
public class EventSegment extends Event
{
	int sequence;
	int total;
	Buffer content;
	
	@Override
    public boolean onDecode(Buffer buffer)
    {
	    // TODO Auto-generated method stub
	    return false;
    }

	@Override
    public boolean onEncode(Buffer buffer)
    {
	    // TODO Auto-generated method stub
	    return false;
    }

	
}
