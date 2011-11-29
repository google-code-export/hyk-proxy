/**
 * 
 */
package org.hyk.proxy.gae.server.handler;

import java.io.IOException;

import org.arch.event.Event;
import org.arch.event.EventHandler;
import org.arch.event.EventHeader;
import org.arch.event.http.HTTPEventContants;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.gae.server.util.GAEServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.ResponseTooLargeException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * @author qiyingwang
 * 
 */
public class ServerProxyEventHandler implements EventHandler
{
	protected Logger logger = LoggerFactory.getLogger(getClass());;
	protected URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
	protected CapabilitiesService service = CapabilitiesServiceFactory
	        .getCapabilitiesService();
	protected AsyncMemcacheService asyncCache = MemcacheServiceFactory
	        .getAsyncMemcacheService();
	protected AccountServiceHandler accountHandler = new AccountServiceHandler();
	protected FetchServiceHandler fetchHandler = new FetchServiceHandler(accountHandler);

	@Override
	public void onEvent(EventHeader header, Event event)
	{
		switch (header.type)
		{
			case HTTPEventContants.HTTP_REQUEST_EVENT_TYPE:
			{
				Event res = fetchHandler.fetch((HTTPRequestEvent) event);
				break;
			}
			case Event.RESERVED_SEGMENT_EVENT_TYPE:
			{
				break;
			}
			default:
				break;
		}
	}

	public boolean init()
	{
		urlFetchService = URLFetchServiceFactory.getURLFetchService();
		return true;
	}

	
}
