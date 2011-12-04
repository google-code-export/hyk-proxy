/**
 * 
 */
package org.hyk.proxy.gae.server.handler;


import java.net.MalformedURLException;

import org.arch.event.Event;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;
import org.hyk.proxy.gae.common.EventHeaderTags;
import org.hyk.proxy.gae.common.auth.User;
import org.hyk.proxy.gae.common.config.GAEServerConfiguration;
import org.hyk.proxy.gae.common.http.RangeHeaderValue;
import org.hyk.proxy.gae.server.service.ServerConfigurationService;
import org.hyk.proxy.gae.server.service.UserManagementService;
import org.hyk.proxy.gae.server.util.GAEServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.apphosting.api.ApiProxy.OverQuotaException;

/**
 * @author qiyingwang
 * 
 */
public class FetchServiceHandler
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private CapabilitiesService capabilities = CapabilitiesServiceFactory
	        .getCapabilitiesService();
	protected URLFetchService urlFetchService = URLFetchServiceFactory
	        .getURLFetchService();

	public FetchServiceHandler()
	{
	}
	
	private void fillErrorResponse(HTTPResponseEvent errorResponse, String cause)
	{
		String str = "You are not allowed to visit this site via proxy because %s.";
		String ret = String.format(str, cause);
		errorResponse.setHeader("Content-Type", "text/plain");
		errorResponse.setHeader("Content-Length", "" + ret.length());
		errorResponse.content.write(ret.getBytes());
	}

	public Event fetch(HTTPRequestEvent req)
	{
		Event ret = null;
		HTTPResponseEvent errorResponse = new HTTPResponseEvent();
		if(capabilities.getStatus(Capability.URL_FETCH).getStatus().equals(CapabilityStatus.DISABLED))
		{
			errorResponse.statusCode = 503;
			fillErrorResponse(errorResponse, "URL Fetch service is no available in maintain time.");
			return errorResponse;
		}
		Object[] attachment = (Object[]) req.getAttachment();
		EventHeaderTags tags = (EventHeaderTags) attachment[0];
		if (UserManagementService.userAuthServiceAvailable(tags.token))
		{
			User user = UserManagementService.getUserWithToken(tags.token);
			if (null == user)
			{		
				errorResponse.statusCode = 401;
				fillErrorResponse(errorResponse, "You are not a authorized user for this proxy server.");
				return errorResponse;
			}
		}

		HTTPRequest fetchReq = null;
		try
		{
			System.out.println("############Fetch url" + req.method +" " + req.url);
			fetchReq = GAEServerHelper.toHTTPRequest(req);
		}
		catch (MalformedURLException e)
		{
			errorResponse.statusCode = 400;
			fillErrorResponse(errorResponse, "Invalid fetch url:" + req.url);
			return errorResponse;
		}
		HTTPResponse fetchRes = null;
		GAEServerConfiguration cfg = ServerConfigurationService.getServerConfig();
		int retry = cfg.getFetchRetryCount();
		do
		{
			try
            {
	            fetchRes = urlFetchService.fetch(fetchReq);
	            ret = GAEServerHelper.toHttpResponseExchange(fetchRes);
            }
			catch(OverQuotaException e)
			{
				errorResponse.statusCode = 503;
				fillErrorResponse(errorResponse, "Over daily quota limit.");
				return errorResponse;
			}
            catch (Exception e)
            {
            	logger.error("Failed to fetch URL:" +req.url , e);
            	retry--;
            	if(!req.containsHeader("Range"))
            	{
            		HTTPHeader rangeHeader = new HTTPHeader("Range", new RangeHeaderValue(0, cfg.getRangeFetchLimit() - 1).toString());
            		fetchReq.addHeader(rangeHeader);
            	}
            }	
		}
		while (null == ret && retry > 0);
		
		if(null == fetchRes)
		{
			errorResponse.statusCode = 408;
			fillErrorResponse(errorResponse, "Fetch timeout for url:" + req.url);
			System.out.println("############@@@@@@@@Timeout for url" + req.url);
			ret = errorResponse;
		}
		return ret;
	}
}
