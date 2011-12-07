/**
 * 
 */
package org.hyk.proxy.gae.common.event;

import java.io.IOException;
import java.util.List;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.auth.Group;
import org.hyk.proxy.gae.common.auth.Operation;
import org.hyk.proxy.gae.common.auth.User;

/**
 * @author qiyingwang
 *
 */
@EventType(GAEConstants.GROUOP_LIST_RESPONSE_EVENT_TYPE)
@EventVersion(1)
public class ListGroupResponseEvent extends Event
{
	public List<Group> groups;
	@Override
    protected boolean onDecode(Buffer buffer)
    {
		try
        {
	        groups = BufferHelper.readList(buffer, Group.class);
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
		BufferHelper.writeList(buffer, groups);
		return true;
    }
}
