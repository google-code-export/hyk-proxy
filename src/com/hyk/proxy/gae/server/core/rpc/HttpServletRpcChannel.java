/**
 * 
 */
package com.hyk.proxy.gae.server.core.rpc;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.common.HttpServerAddress;
import com.hyk.rpc.core.address.SimpleSockAddress;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.util.buffer.ByteArray;

/**
 * @author Administrator
 * 
 */
public class HttpServletRpcChannel extends AbstractAppEngineRpcChannel
{
	protected Logger								logger						= LoggerFactory.getLogger(getClass());

	private static ThreadLocal<HttpServletResponse>	httpServletResponseCache	= new ThreadLocal<HttpServletResponse>();

	private HttpServerAddress						address;

	public HttpServletRpcChannel(HttpServerAddress address)
	{
		super();
		this.address = address;
	}

	@Override
	public HttpServerAddress getRpcChannelAddress()
	{
		return address;
	}

	@Override
	protected void send(RpcChannelData data) throws IOException
	{
		ByteArray content = data.content;
		if(logger.isDebugEnabled())
		{
			logger.debug("Send result back with body len:" + data.content.size());
		}
		httpServletResponseCache.get().setStatus(200);
		httpServletResponseCache.get().setContentLength(data.content.size());
		httpServletResponseCache.get().getOutputStream().write(content.rawbuffer(), content.position(), content.size());
	}

	public void processHttpRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		httpServletResponseCache.set(response);

		try
		{
			int contentLen = request.getContentLength();
			if(contentLen > 0)
			{
				byte[] content = new byte[contentLen];
				int readLen = 0;
				int left = contentLen;
				while(left > 0)
				{
					readLen = request.getInputStream().read(content, contentLen - left, left);
					left -= readLen;
				}
				ByteArray buffer = ByteArray.wrap(content);
				RpcChannelData recv = new RpcChannelData(buffer, new SimpleSockAddress(request.getRemoteHost(), request.getRemotePort()));
				processIncomingData(recv);
			}

		}
		catch(Exception e)
		{
			CharArrayWriter writer = new CharArrayWriter();
			e.printStackTrace(new PrintWriter(writer));
			response.getWriter().write(writer.toString());
		}
	}

	@Override
	public boolean isReliable()
	{
		return true;
	}
}
