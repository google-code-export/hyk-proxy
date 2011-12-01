/**
 * 
 */
package org.hyk.proxy.gae.server.handler;

import java.io.IOException;

import org.arch.event.Event;
import org.arch.event.http.HTTPRequestEvent;
import org.hyk.proxy.gae.common.EventHeaderTags;
import org.hyk.proxy.gae.common.auth.User;
import org.hyk.proxy.gae.server.service.UserManagementService;
import org.hyk.proxy.gae.server.util.GAEServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	protected Logger logger = LoggerFactory.getLogger(getClass());
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
			Object[] attachment = (Object[]) req.getAttachment();
			EventHeaderTags tags = (EventHeaderTags) attachment[0];
			if(UserManagementService.userAuthServiceAvailable(tags.token))
			{
				User user = UserManagementService.getUserWithToken(tags.token);
				
			}
			
			
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
