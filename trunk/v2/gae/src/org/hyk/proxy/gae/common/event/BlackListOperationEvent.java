/**
 * 
 */
package org.hyk.proxy.gae.common.event;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.auth.Operation;
import org.hyk.proxy.gae.common.auth.User;

/**
 * @author qiyingwang
 *
 */
@EventType(GAEConstants.BLACKLIST_OPERATION_EVENT_TYPE)
@EventVersion(1)
public class BlackListOperationEvent extends Event
{
	public String username;
	public String groupname;
	public String host;
	public Operation opr;
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
