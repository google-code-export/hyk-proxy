/**
 * 
 */
package org.arch.event.http;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.event.EventType;
import org.arch.event.EventVersion;

/**
 * @author qiyingwang
 *
 */
@EventType(HTTPEventContants.HTTP_REQUEST_EVENT_TYPE)
@EventVersion(1)
public class HTTPRequestEvent extends HTTPMessageEvent
{
	public String url;
	public String method;
	@Override
    protected boolean doDecode(Buffer buffer)
    {
		try
        {
			url = BufferHelper.readVarString(buffer);
			method = BufferHelper.readVarString(buffer);
        }
        catch (Exception e)
        {
	        return false;
        }
	    return true;
    }
	@Override
    protected boolean doEncode(Buffer buffer)
    {
	    BufferHelper.writeVarString(buffer, url);
	    BufferHelper.writeVarString(buffer, method);
	    return true;
    }
	
	@Override
	public String toString()
	{
	    StringBuilder buffer = new StringBuilder();
	    buffer.append(method).append(" ").append(url).append("\r\n");
	    toString(buffer);
	    return buffer.toString();
	}
	
}
