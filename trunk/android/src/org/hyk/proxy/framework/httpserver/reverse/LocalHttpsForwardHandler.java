/**
 * This file is part of the hyk-proxy-android project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: LocalHttpsForwardHandler.java 
 *
 * @author yinqiwen [ 2011-7-10 | ÏÂÎç06:46:07 ]
 *
 */
package org.hyk.proxy.framework.httpserver.reverse;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;

import org.hyk.proxy.framework.common.Misc;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;


/**
 *
 */
@ChannelPipelineCoverage("one")
public class LocalHttpsForwardHandler extends SimpleChannelUpstreamHandler
{
	private Channel localChannel;
	private Channel remoteChannel;
	private static ClientSocketChannelFactory factory = null;

	public LocalHttpsForwardHandler()
	{
		if (null == factory)
		{
			factory = new NioClientSocketChannelFactory(
			        Misc.getGlobalThreadPool(), Misc.getGlobalThreadPool());
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
	        throws Exception
	{
	   if(null != remoteChannel)
	   {
		   remoteChannel.close();
		   remoteChannel = null;
	   }
	}
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
	        throws Exception
	{
		localChannel = ctx.getChannel();
		if (null == remoteChannel)
		{
			ChannelPipeline pipeline = pipeline();
			pipeline.addLast("fr", new RemoteForwardLocalHandler());
			remoteChannel = factory.newChannel(pipeline);
			remoteChannel.connect(HttpsReverseServer.getInstance().getReverseServerSocketAddress()).awaitUninterruptibly();
		}
		Object mesage = e.getMessage();
		if (mesage instanceof ChannelBuffer)
		{
			ChannelBuffer bufmsg = (ChannelBuffer) mesage;
			remoteChannel.write(mesage);
		}
	}

	@ChannelPipelineCoverage("one")
	class RemoteForwardLocalHandler extends SimpleChannelUpstreamHandler
	{
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
		        throws Exception
		{
		    if(null != localChannel)
		    {
		    	localChannel.close();
		    	localChannel = null;
		    }
		}
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		        throws Exception
		{
		    if(null != localChannel)
		    {
		    	localChannel.write(e.getMessage());
		    }
		}
	}
}
