/**
 * 
 */
package org.hyk.proxy.gae.server.handler;


import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventHandler;
import org.arch.event.EventHeader;
import org.arch.event.http.HTTPEventContants;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.gae.common.EventHeaderTags;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.GAEEventHelper;
import org.hyk.proxy.gae.server.service.EventSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * @author qiyingwang
 * 
 */
public class ServerProxyEventHandler implements EventHandler
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
	
	protected AsyncMemcacheService asyncCache = MemcacheServiceFactory
	        .getAsyncMemcacheService();
	protected AccountServiceHandler accountHandler = new AccountServiceHandler();
	protected FetchServiceHandler fetchHandler = new FetchServiceHandler();

	@Override
	public void onEvent(EventHeader header, Event event)
	{
		Event response = null;
		Object[] attach = (Object[]) event.getAttachment();
		EventHeaderTags tags = (EventHeaderTags) attach[0];
		EventSendService sendService = (EventSendService) attach[1];
		switch (header.type)
		{
			case GAEConstants.USER_OPERATION_EVENT_TYPE:
			case GAEConstants.GROUP_OPERATION_EVENT_TYPE:
			case GAEConstants.USER_LIST_REQUEST_EVENT_TYPE:
			case GAEConstants.GROUOP_LIST_REQUEST_EVENT_TYPE:
			case GAEConstants.USER_LIST_RESPONSE_EVENT_TYPE:
			case GAEConstants.GROUOP_LIST_RESPONSE_EVENT_TYPE:
			case GAEConstants.BLACKLIST_OPERATION_EVENT_TYPE:
			{
				response = accountHandler.handleEvent(header.type, event);
				break;
			}
			case HTTPEventContants.HTTP_REQUEST_EVENT_TYPE:
			{
				response = fetchHandler.fetch((HTTPRequestEvent) event);
				break;
			}
			case Event.RESERVED_SEGMENT_EVENT_TYPE:
			{
				break;
			}
			default:
				break;
		}
		if(null != response)
		{
			response.setHash(event.getHash());
			Buffer buf = GAEEventHelper.encodeEvent(tags, response);
			if(sendService.getMaxDataPackageSize() > 0 && buf.readableBytes() > sendService.getMaxDataPackageSize())
			{
				Buffer[] bufs = GAEEventHelper.splitEventBuffer(buf, event.getHash(), sendService.getMaxDataPackageSize(), tags);
				for (Buffer buffer : bufs)
                {
					sendService.send(buffer);
                }
			}
			else
			{
				sendService.send(buf);
			}
		}
	}
	
}
