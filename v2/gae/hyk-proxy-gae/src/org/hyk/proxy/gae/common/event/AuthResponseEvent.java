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
@EventType(GAEConstants.AUTH_REQUEST_EVENT_TYPE)
@EventVersion(1)
public class AuthResponseEvent extends Event
{
	public String appid;
	public String token;
	public String error;

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
