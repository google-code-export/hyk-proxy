/**
 * 
 */
package com.hyk.proxy.gae.client.netty;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
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
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.hyk.compress.Compressor;
import com.hyk.compress.sevenzip.SevenZipCompressor;
import com.hyk.proxy.gae.client.XmppTalk;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;


/**
 * @author Administrator
 *
 */
@ChannelPipelineCoverage("one")
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {
	static Compressor 	compressor = new SevenZipCompressor();
	static Serializer serializer = new HykSerializer();
    private volatile HttpRequest request;
    private volatile boolean readingChunks;
    private final StringBuilder responseContent = new StringBuilder();
    
    protected HttpRequestExchange buildForwardRequest(HttpRequest request) throws IOException
	{
		HttpRequestExchange gaeRequest = new HttpRequestExchange();
		StringBuffer urlbuffer = new StringBuffer();
		urlbuffer.append(request.getUri());
		//urlbuffer.append("?").append(request.getQueryString());
		gaeRequest.setURL(urlbuffer.toString());
		gaeRequest.setMethod(request.getMethod().getName());
		Set<String> headers = request.getHeaderNames();
		for(String headerName:headers)
		{
			List<String> headerValues = request.getHeaders(headerName);
			if(null != headerValues)
			{
				for (String headerValue : headerValues) {
					gaeRequest.addHeader(headerName, headerValue);
				}
			}
		}

		int bodyLen = (int) request.getContentLength();
		
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
    	HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(forwardResponse.getResponseCode()));
		//response.setStatus(forwardResponse.getResponseCode());
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
			//response.getOutputStream().write(content);
		}
		return response;
	}
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!readingChunks) {
            HttpRequest request = this.request = (HttpRequest) e.getMessage();
            HttpRequestExchange forwardRequest = buildForwardRequest(request);
            byte[] data = serializer.serialize(forwardRequest);
			data = compressor.compress(data);

			byte[] resContent = new XmppTalk().talk(data);
			resContent = compressor.decompress(resContent);
			HttpResponseExchange forwardResponse = serializer.deserialize(HttpResponseExchange.class, resContent);
			HttpResponse response = buildHttpServletResponse(forwardResponse);
			ChannelFuture future = e.getChannel().write(response);

	        // Close the connection after the write operation is done if necessary.
			future.addListener(ChannelFutureListener.CLOSE);
			if (request.isChunked()) {
                readingChunks = true;
            } else {
               
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                responseContent.append("END OF CONTENT\r\n");
                writeResponse(e);
            } else {
                responseContent.append("CHUNK: " + chunk.getContent().toString("UTF-8") + "\r\n");
            }
        }
    }

    private void writeResponse(MessageEvent e) {
        // Convert the response content to a ChannelBuffer.
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent.toString(), "UTF-8");
        responseContent.setLength(0);

        // Decide whether to close the connection or not.
        boolean close =
            HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) ||
            request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) &&
            !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.setContent(buf);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
        }

        String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
        if (cookieString != null) {
            CookieDecoder cookieDecoder = new CookieDecoder();
            Set<Cookie> cookies = cookieDecoder.decode(cookieString);
            if(!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                CookieEncoder cookieEncoder = new CookieEncoder(true);
                for (Cookie cookie : cookies) {
                    cookieEncoder.addCookie(cookie);
                }
                response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
            }
        }

        // Write the response.
        ChannelFuture future = e.getChannel().write(response);

        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
