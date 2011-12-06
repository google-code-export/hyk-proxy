/**
 * This file is part of the hyk-proxy-spac project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SocksForwardProxyEventService.java 
 *
 * @author yinqiwen [ 2011-5-12 | ÏÂÎç08:31:18 ]
 *
 */
package org.hyk.proxy.spac.handler.forward;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import org.arch.event.Event;
import org.arch.event.EventHeader;
import org.arch.event.NamedEventHandler;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.jsocks.socks.Proxy;
import net.sourceforge.jsocks.socks.Socks4Proxy;
import net.sourceforge.jsocks.socks.Socks5Proxy;
import net.sourceforge.jsocks.socks.SocksException;
import net.sourceforge.jsocks.socks.SocksSocket;

/**
 *
 */
public class SocksForwardProxyEventService implements NamedEventHandler
{

	@Override
    public void onEvent(EventHeader header, Event event)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public String getName()
    {
	    return "SOCKS";
    }
	
}
