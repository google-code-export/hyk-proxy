/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Main.java 
 *
 * @author Administrator [ 2010-1-30 | ÉÏÎç10:23:38 ]
 *
 */
package com.hyk.proxy.gae.client;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.SocketChannel;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.util.buffer.ByteArray;

/**
 *
 */
public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ClientSocketChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("encoder", new HttpRequestEncoder());
		pipeline.addLast("handler", new HttpResponseHandler());
		SocketChannel channel = factory.newChannel(pipeline);
		channel.connect(new InetSocketAddress("localhost", 8888));
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/fetchproxy");
		request.setHeader("Host", "127.0.0.1:8888");	
		//request.setHeader(HttpHeaders.Names.USER_AGENT, "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.5) Gecko/20091102 Firefox/3.5.5 QQDownload/1.7");
		//request.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
		//request.setHeader("Keep-Alive", "115");
		//request.setHeader("Cache-Control", "max-age=0");
		//request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		//request.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
		//request.setHeader(HttpHeaders.Names.CONTENT_TRANSFER_ENCODING, HttpHeaders.Values.BINARY);
		
		
		channel.write(request).awaitUninterruptibly();
		System.out.println("####Send success!");
	}
	
	 @ChannelPipelineCoverage("one")
	static class HttpResponseHandler extends SimpleChannelUpstreamHandler
	{
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
		{
			System.out.println("Close socket");
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
		{
			if(!(e.getMessage() instanceof HttpResponse))
			{
				HttpChunk chunk = (HttpChunk)e.getMessage();
				System.out.println("### " +chunk.getContent().toString("UTF-8"));
				return;
			}
			HttpResponse response = (HttpResponse)e.getMessage();

			int bodyLen = (int)response.getContentLength();
			System.out.println("Recv message: " + response.toString());
			System.out.println("Recv message with len " + bodyLen);
			if(bodyLen > 0)
			{
				ByteArray content = ByteArray.allocate(bodyLen);
				ChannelBuffer body = response.getContent();
				body.readBytes(content.buffer());

			}
			else
			{

			}
		}
	}

}
