/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyServlet.java 
 *
 * @author yinqiwen [ 2010-1-29 | pm09:58:01 ]
 *
 */
package org.hyk.proxy.gae.server.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventDispatcher;
import org.hyk.proxy.gae.common.EventHeaderTags;
import org.hyk.proxy.gae.common.GAEEventHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.hyk.proxy.gae.server.core.Launcher;

/**
 *
 */
public class HttpInvokeServlet extends HttpServlet
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	        throws IOException
	{
		try
		{
			int bodylen = req.getContentLength();
			if (bodylen > 0)
			{
				Buffer content = new Buffer(bodylen);
				int len = content.read(req.getInputStream());
				if (len > 0)
				{
					EventHeaderTags tags = new EventHeaderTags();
					Event event = GAEEventHelper.parseEvent(content, tags);
					event.setAttachment(new Object[] { tags, resp });
					EventDispatcher.getSingletonInstance().dispatch(event);
				}
			}
		}
		catch (Throwable e)
		{
			logger.warn("Failed to process message", e);
		}
	}

}
