/**
 * 
 */
package com.hyk.proxy.server.gae.rpc.channel;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.proxy.common.http.message.HttpServerAddress;
import com.hyk.proxy.common.secure.SecurityServiceFactory;
import com.hyk.proxy.common.secure.SecurityServiceFactory.RegistSecurityService;
import com.hyk.proxy.server.gae.config.XmlConfig;
import com.hyk.rpc.core.address.SimpleSockAddress;
import com.hyk.rpc.core.transport.RpcChannelData;

/**
 * @author yinqiwen
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
		ChannelDataBuffer content = data.content;
		if(logger.isDebugEnabled())
		{
			logger.debug("Send result back with body len:" + data.content.readableBytes());
		}
		HttpServletResponse out = httpServletResponseCache.get();
		out.setStatus(200);
		out.setContentLength(content.readableBytes() + 4);
		RegistSecurityService reg = SecurityServiceFactory.getRegistSecurityService(XmlConfig.getInstance().getHttpDownStreamEncrypter());
		ByteBuffer secid = ByteBuffer.allocate(4);
		secid.putInt(reg.id);
		out.getOutputStream().write(secid.array());
		ByteBuffer[] bufs = ChannelDataBuffer.asByteBuffers(content);
		//byte[] sent = ChannelDataBuffer.asByteArray(content);
		bufs = reg.service.encrypt(bufs);
		for(ByteBuffer buf:bufs)
		{
			out.getOutputStream().write(buf.array(), buf.position(), buf.remaining());
		}
		out.getOutputStream().flush();
		
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
				ByteBuffer bufferContent = ByteBuffer.wrap(content);
				int secid = bufferContent.getInt();
				RegistSecurityService reg = SecurityServiceFactory.getRegistSecurityService(secid);
				bufferContent = reg.service.decrypt(bufferContent);
				ChannelDataBuffer buffer = ChannelDataBuffer.wrap(bufferContent.array(), bufferContent.position(), bufferContent.remaining());
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
