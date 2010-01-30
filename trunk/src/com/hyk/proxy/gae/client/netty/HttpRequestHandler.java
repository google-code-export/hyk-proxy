/**
 * 
 */
package com.hyk.proxy.gae.client.netty;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jivesoftware.smack.XMPPException;

import com.hyk.compress.Compressor;
import com.hyk.compress.NonCompressor;
import com.hyk.proxy.gae.client.xmpp.XmppRpcChannel;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.proxy.gae.common.XmppAddress;
import com.hyk.proxy.gae.common.service.FetchService;
import com.hyk.rpc.core.RPC;
import com.hyk.rpc.core.service.NameService;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;

/**
 * @author Administrator
 * 
 */
@ChannelPipelineCoverage("one")
public class HttpRequestHandler extends SimpleChannelUpstreamHandler
{
	//static Compressor				compressor		= new NonCompressor();
	//static Serializer				serializer		= new HykSerializer();
	static SSLContext				sslContext;

	static
	{
		try
		{
			String password = "hykproxy";
			sslContext = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(new FileInputStream("hykproxykeystore"), password.toCharArray());
			kmf.init(ks, password.toCharArray());
			KeyManager[] km = kmf.getKeyManagers();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);
			TrustManager[] tm = tmf.getTrustManagers();
			sslContext.init(km, tm, null);
		}
		catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private volatile HttpRequest	request;
	private volatile boolean		readingChunks;
	private final StringBuilder		responseContent	= new StringBuilder();
	private ChannelPipeline			channelPipeline;

	private boolean					ishttps			= false;
	private String					httpspath		= null;

	private FetchService			fetchService;

	public HttpRequestHandler(ChannelPipeline channelPipeline, FetchService	fetchService)
	{
		this.channelPipeline = channelPipeline;
		this.fetchService = fetchService;
	}

	protected HttpRequestExchange buildForwardRequest(HttpRequest request) throws IOException
	{
		HttpRequestExchange gaeRequest = new HttpRequestExchange();
		StringBuffer urlbuffer = new StringBuffer();
		if(ishttps)
		{
			urlbuffer.append("https://").append(httpspath);
		}
		urlbuffer.append(request.getUri());
		// urlbuffer.append("?").append(request.getQueryString());
		gaeRequest.setURL(urlbuffer.toString());
		gaeRequest.setMethod(request.getMethod().getName());
		Set<String> headers = request.getHeaderNames();
		for(String headerName : headers)
		{
			List<String> headerValues = request.getHeaders(headerName);
			if(null != headerValues)
			{
				for(String headerValue : headerValues)
				{
					gaeRequest.addHeader(headerName, headerValue);
				}
			}
		}

		int bodyLen = (int)request.getContentLength();

		if(bodyLen > 0)
		{
			byte[] payload = new byte[bodyLen];
			ChannelBuffer body = request.getContent();
			body.readBytes(payload);
			gaeRequest.setBody(payload);
		}
		return gaeRequest;
	}

	protected HttpResponse buildHttpServletResponse(HttpResponseExchange forwardResponse) throws IOException
	{
		
		if(null == forwardResponse)
		{
			return new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT);
		}
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(forwardResponse.getResponseCode()));
		// response.setStatus(forwardResponse.getResponseCode());
		List<String[]> headers = forwardResponse.getHeaders();
		for(String[] header : headers)
		{
			response.setHeader(header[0], header[1]);
		}
		byte[] content = forwardResponse.getBody();
		if(null != content)
		{
			ChannelBuffer bufer = ChannelBuffers.copiedBuffer(content);
			response.setContent(bufer);
			// response.getOutputStream().write(content);
		}
		return response;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
	{
		if(!readingChunks)
		{
			HttpRequest request = this.request = (HttpRequest)e.getMessage();
			System.out.println("####" + request.getMethod() + " " + request.getUri());
			if(request.getMethod().equals(HttpMethod.CONNECT))
			{
				ishttps = true;
				httpspath = request.getHeader("Host");
				HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
				e.getChannel().write(response).await(10000);
				if(channelPipeline.get("ssl") == null)
				{
					InetSocketAddress remote = (InetSocketAddress)e.getRemoteAddress();
					SSLEngine engine = sslContext.createSSLEngine(remote.getAddress().getHostAddress(), remote.getPort());
					engine.setUseClientMode(false);
					channelPipeline.addBefore("decoder", "ssl", new SslHandler(engine));

				}
				return;
			}
			HttpRequestExchange forwardRequest = buildForwardRequest(request);
			//forwardRequest.printMessage();
			HttpResponseExchange forwardResponse = fetchService.fetch(forwardRequest);
			HttpResponse response = buildHttpServletResponse(forwardResponse);
			//forwardResponse.printMessage();
			ChannelFuture future = e.getChannel().write(response);

			// Close the connection after the write operation is done if
			// necessary.
			future.addListener(ChannelFutureListener.CLOSE);
			if(request.isChunked())
			{
				readingChunks = true;
			}
			else
			{

			}
		}
		else
		{
			HttpChunk chunk = (HttpChunk)e.getMessage();
			if(chunk.isLast())
			{
				readingChunks = false;
				responseContent.append("END OF CONTENT\r\n");
				writeResponse(e);
			}
			else
			{
				responseContent.append("CHUNK: " + chunk.getContent().toString("UTF-8") + "\r\n");
			}
		}
	}

	private void writeResponse(MessageEvent e)
	{
		// Convert the response content to a ChannelBuffer.
		ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent.toString(), "UTF-8");
		responseContent.setLength(0);

		// Decide whether to close the connection or not.
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION))
				|| request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
				&& !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.setContent(buf);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

		if(!close)
		{
			// There's no need to add 'Content-Length' header
			// if this is the last response.
			response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
		}

		String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
		if(cookieString != null)
		{
			CookieDecoder cookieDecoder = new CookieDecoder();
			Set<Cookie> cookies = cookieDecoder.decode(cookieString);
			if(!cookies.isEmpty())
			{
				// Reset the cookies if necessary.
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				for(Cookie cookie : cookies)
				{
					cookieEncoder.addCookie(cookie);
				}
				response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
			}
		}

		// Write the response.
		ChannelFuture future = e.getChannel().write(response);

		// Close the connection after the write operation is done if necessary.
		if(close)
		{
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
	{
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
