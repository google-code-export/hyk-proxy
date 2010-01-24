package com.hyk.proxy.gae.server;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hyk.util.buffer.ByteArray;


@SuppressWarnings("serial")
public class FetchProxyServlet extends HttpServlet
{
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		int len = req.getContentLength();
		byte[] buffer = new byte[len];
		req.getInputStream().read(buffer);
		try
		{
			ByteArray rawRes = FatchServiceWrapper.fetch(ByteArray.wrap(buffer));
			resp.getOutputStream().write(rawRes.rawbuffer(), rawRes.position(), rawRes.size());
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			resp.setStatus(503);
			resp.setContentType("text/plain");
			resp.getWriter().println("####Failed " + Arrays.toString(e.getStackTrace()));
		}
	}
}
