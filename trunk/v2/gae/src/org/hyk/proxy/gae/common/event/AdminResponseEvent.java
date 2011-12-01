/**
 * 
 */
package org.hyk.proxy.gae.common.event;

import org.arch.buffer.Buffer;
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
