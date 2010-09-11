/**
 * 
 */
package com.hyk.proxy.plugin.phptunnel.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.event.tunnel.AbstractTunnelProxyEventService.CallBack;


/**
 * @author wqy
 * 
 */
@ChannelPipelineCoverage("one")
public class PhpTunnelLocalServerHandler extends SimpleChannelUpstreamHandler
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private static Map<String, CallBack> table = new ConcurrentHashMap<String, CallBack>();
	
	public  static void registerCallBack(String id, CallBack callback)
	{
		table.put(id, callback);
	}
	
	public  static void removeCallBack(String id)
	{
		table.remove(id);
	}
	
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
	        throws Exception
	{
		Channel channel = e.getChannel();
		if(e.getMessage() instanceof ChannelBuffer)
		{
			ChannelBuffer buf = (ChannelBuffer) e.getMessage();
			byte[] array = new byte[10];
			buf.readBytes(array);
			String id = new String(array);
			if (logger.isDebugEnabled())
			{
				logger.debug("Recv a session id:" + id);
			}
			channel.getPipeline().remove("acceptor");
			if(table.containsKey(id))
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Invoke callback by session id:" + id);
				}
				table.remove(id).callback(channel);
			}
		}
	}

}
