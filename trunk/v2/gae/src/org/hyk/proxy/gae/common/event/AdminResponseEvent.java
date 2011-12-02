/**
 * 
 */
package org.hyk.proxy.gae.common.event;

import java.io.IOException;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;
import org.hyk.proxy.gae.common.GAEConstants;

/**
 * @author qiyingwang
 * 
 */
@EventType(GAEConstants.ADMIN_RESPONSE_EVENT_TYPE)
@EventVersion(1)
public class AdminResponseEvent extends Event
{
	public String response;
	public String errorCause;
	public int errno;

	@Override
	protected boolean onDecode(Buffer buffer)
	{
		try
        {
			response = BufferHelper.readVarString(buffer);
			errorCause = BufferHelper.readVarString(buffer);
			errno = BufferHelper.readVarInt(buffer);
			return true;
        }
        catch (IOException e)
        {
	        return false;
        }
	}

	@Override
	protected boolean onEncode(Buffer buffer)
	{
		BufferHelper.writeVarString(buffer, response);
		BufferHelper.writeVarString(buffer, errorCause);
		BufferHelper.writeVarInt(buffer, errno);
		return true;
	}

}
