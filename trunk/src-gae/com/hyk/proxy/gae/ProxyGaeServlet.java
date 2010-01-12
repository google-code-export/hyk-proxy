package com.hyk.proxy.gae;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.*;

import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.users.UserServiceFactory;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;
import com.hyk.serializer.StandardSerializer;

@SuppressWarnings("serial")
public class ProxyGaeServlet extends HttpServlet
{
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		int len = req.getContentLength();
		byte[] buffer = new byte[len];
		req.getInputStream().read(buffer);
			
		try
		{
			Serializer serializer = new StandardSerializer();
			HTTPRequest fetchReq = serializer.deserialize(HTTPRequest.class, buffer);
			HTTPResponse fetchRes = URLFetchServiceFactory.getURLFetchService().fetch(fetchReq);
			resp.setStatus(200);
			resp.getOutputStream().write(serializer.serialize(fetchRes));
		}
		catch(Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			resp.setStatus(503);
			resp.setContentType("text/plain");
			resp.getWriter().println("####Failed " + Arrays.toString(e.getStackTrace()));
		}
		
	}
}
