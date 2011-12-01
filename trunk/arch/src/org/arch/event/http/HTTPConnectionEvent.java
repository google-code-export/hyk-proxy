/**
 * 
 */
package org.arch.event.http;

import java.io.IOException;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;

/**
 * @author qiyingwang
 * 
 */
@EventType(HTTPEventContants.HTTP_CONNECTION_EVENT_TYPE)
@EventVersion(1)
public  class HTTPConnectionEvent extends Event
{
	public static final int OPEND = 1;
	public static final int CLOSED = 2;
	
	public int status;
	public HTTPConnectionEvent(int status)
    {
	    this.status = status;
    }

	@Override
    protected boolean onDecode(Buffer buffer)
    {
		BufferHelper.writeVarInt(buffer, status);
	    return true;
    }

	@Override
    protected boolean onEncode(Buffer buffer)
    {
		try
        {
	        status = BufferHelper.readVarInt(buffer);
        }
        catch (IOException e)
        {
	        return false;
        }
	    return true;
    }
	

}
