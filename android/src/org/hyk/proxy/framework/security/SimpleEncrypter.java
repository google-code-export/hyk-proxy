/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SimpleEncrypter.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:25:47 AM ]
 *
 */
package org.hyk.proxy.framework.security;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 *
 */
public class SimpleEncrypter
{
	static SimpleSecurityService serv = new SimpleSecurityService();
	
	@ChannelPipelineCoverage("one")
	public static class SimpleEncryptEncoder extends OneToOneEncoder
	{
		@Override
		protected Object encode(ChannelHandlerContext arg0, Channel arg1, Object msg) throws Exception
		{
			ChannelBuffer buffer = (ChannelBuffer)msg;
			int len = buffer.readableBytes();
			for(int i = 0; i < len; i++)
			{
				buffer.setByte(i, serv.encrypt(buffer.getByte(i)));
			}
			//buffer.
			return buffer;
		}
		
	}
	
	@ChannelPipelineCoverage("one")
	public static class SimpleDecryptDecoder extends OneToOneDecoder
	{
		@Override
		protected Object decode(ChannelHandlerContext arg0, Channel arg1, Object msg) throws Exception
		{
			ChannelBuffer buffer = (ChannelBuffer)msg;
			int len = buffer.readableBytes();
			for(int i = 0; i < len; i++)
			{
				buffer.setByte(i, serv.decrypt(buffer.getByte(i)));
			}
			//buffer.
			return buffer;
		}
		
	}
}
