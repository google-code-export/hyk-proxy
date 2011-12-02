/**
 * 
 */
package org.hyk.proxy.gae.common.event;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.config.GAEServerConfiguration;

/**
 * @author qiyingwang
 *
 */
@EventType(GAEConstants.SERVER_CONFIG_REQUEST_EVENT_TYPE)
@EventVersion(1)
public class ServerConfigRequestEvent extends Event
{
	public GAEServerConfiguration cfg;
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
