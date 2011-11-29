/**
 * 
 */
package org.hyk.proxy.gae.server.handler;

import java.io.IOException;

import org.arch.event.Event;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.gae.server.util.GAEServerHelper;

import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.ResponseTooLargeException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * @author qiyingwang
 *
 */
public class FetchServiceHandler
{
	private AccountServiceHandler accountService;
	protected URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
	
	public FetchServiceHandler(AccountServiceHandler accountService)
    {
	    this.accountService = accountService;
    }
	
	public Event fetch(HTTPRequestEvent req)
	{
		Event ret = null;
		try
		{
			// if(!authByBlacklist(req))
			// {
			// return createErrorResponse("blacklist restriction");
			// }
			// if(!authByTrafficRestriction(req))
			// {
			// return createErrorResponse("your account:" + user.getEmail() +
			// " has exceed the traffic limit today for the proxyed host:"
			// + req.getHeaderValue("Host"));
			// }
			HTTPRequest fetchReq = GAEServerHelper.toHTTPRequest(req);
			HTTPResponse fetchRes = urlFetchService.fetch(fetchReq);
			ret = GAEServerHelper.toHttpResponseExchange(fetchRes);
			// Store this value since the RPC framework would use this value to
			// judge whole message compressing or not

		}
		catch (IOException e)
		{
			logger.error("Faile to fetch", e);
			// ret = new HttpResponseExchange().setResponseTooLarge(true);
		}
		catch (ResponseTooLargeException e)
		{
			// ret = new HttpResponseExchange().setResponseTooLarge(true);
		}
		catch (Throwable e)
		{
			logger.error("Faile to fetch", e);
		}
		return ret;
	}
}
