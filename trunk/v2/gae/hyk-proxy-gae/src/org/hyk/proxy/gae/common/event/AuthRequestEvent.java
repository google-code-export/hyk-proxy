/**
 * 
 */
package org.hyk.proxy.gae.common.event;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;
import org.arch.event.http.HTTPEventContants;

/**
 * @author qiyingwang
 *
 */
@EventType(HTTPEventContants.HTTP_CHUNK_EVENT_TYPE)
@EventVersion(1)
public class AuthRequestEvent extends Event
{
	public String appid;
	public String user;
	public String passwd;
	@Override
    protected boolean onDecode(Buffer buffer)
    {
	    // TODO Auto-generated method stub
	    return false;
    }

	@Override
    protected boolean onEncode(Buffer buffer)
    {
	    // TODO Auto-generated method stub
	    return false;
    }

}
