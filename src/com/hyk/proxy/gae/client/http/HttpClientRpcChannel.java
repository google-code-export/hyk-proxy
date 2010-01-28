/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpClientRpcChannel.java 
 *
 * @author qiying.wang [ Jan 28, 2010 | 5:41:08 PM ]
 *
 */
package com.hyk.proxy.gae.client.http;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.transport.AbstractDefaultRpcChannel;
import com.hyk.rpc.core.transport.RpcChannelData;

/**
 *
 */
public class HttpClientRpcChannel extends AbstractDefaultRpcChannel
{
	public HttpClientRpcChannel(Executor threadPool)
	{
		super(threadPool);
	}

	@Override
	public Address getRpcChannelAddress()
	{
		return null;
	}

	@Override
	protected RpcChannelData read() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void send(RpcChannelData data) throws IOException
	{
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "");
		request.addHeader("connection", "keep-alive");
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data.content.buffer());
		request.setContent(buffer);
		
	}

}
