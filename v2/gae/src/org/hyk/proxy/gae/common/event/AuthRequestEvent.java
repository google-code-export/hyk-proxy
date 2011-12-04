/**
 * 
 */
package org.hyk.proxy.gae.common.event;

import java.io.IOException;
import java.util.Arrays;

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
@EventType(GAEConstants.AUTH_REQUEST_EVENT_TYPE)
@EventVersion(1)
public class AuthRequestEvent extends Event
{
	public String appid;
	public String user;
	public String passwd;
	@Override
    protected boolean onDecode(Buffer buffer)
    {
		try
        {
			System.out.println("####"+buffer.readableBytes());
			System.out.println("####"+Arrays.toString(buffer.toArray()));
	        appid = BufferHelper.readVarString(buffer);
	        user = BufferHelper.readVarString(buffer);
			passwd = BufferHelper.readVarString(buffer);
        }
        catch (IOException e)
        {
	        return false;
        }
	    return true;
    }

	@Override
    protected boolean onEncode(Buffer buffer)
    {
	    BufferHelper.writeVarString(buffer, appid);
	    BufferHelper.writeVarString(buffer, user);
	    BufferHelper.writeVarString(buffer, passwd);
	    Buffer buf = new Buffer();
	    BufferHelper.writeVarString(buf, appid);
	    BufferHelper.writeVarString(buf, user);
	    BufferHelper.writeVarString(buf, passwd);
	    System.out.println("####"+Arrays.toString(buffer.toArray()));
	    return true;
    }

}
