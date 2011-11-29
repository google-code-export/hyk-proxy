/**
 * 
 */
package org.arch.event.http;

import java.io.IOException;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.event.EventType;
import org.arch.event.EventVersion;

/**
 * @author qiyingwang
 *
 */
@EventType(HTTPEventContants.HTTP_RESPONSE_EVENT_TYPE)
@EventVersion(1)
public class HTTPResponseEvent extends HTTPMessageEvent
{
	public int statusCode;

	@Override
    protected boolean doDecode(Buffer buffer)
    {
		try
        {
	        statusCode = BufferHelper.readVarInt(buffer);
        }
        catch (IOException e)
        {
	        return false;
        }
	    return true;
    }

	@Override
    protected boolean doEncode(Buffer buffer)
    {
		BufferHelper.writeVarInt(buffer, statusCode);
	    return true;
    }
	
}
