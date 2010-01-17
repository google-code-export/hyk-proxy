/**
 * 
 */
package com.hyk.proxy.gae.common;

/**
 * @author Administrator
 *
 */
public class XmppMeaageUtil {

	public static class HykProxyXmppRequest
	{
		public final int sessionId;
		public final String body;
		public HykProxyXmppRequest(int id, String body)
		{
			sessionId = id;
			this.body = body;
		}
	}
	
	public static class HykProxyXmppResponse implements Comparable<HykProxyXmppResponse>
	{
		public final int sessionId;
		public final int seq;
		public final String body;
		public HykProxyXmppResponse(int id, int seq, String body)
		{
			sessionId = id;
			this.seq = seq;
			this.body = body;
		}
		@Override
		public int compareTo(HykProxyXmppResponse o) {
			// TODO Auto-generated method stub
			return seq - o.seq;
		}
	}
	
	public static HykProxyXmppRequest parseRequest(String msg)
	{
		int index = msg.indexOf(']');
		String left = msg.substring(index + 1);
		String sessionID = msg.substring(1, index);
		return new HykProxyXmppRequest(Integer.parseInt(sessionID), left);
	}
	
	public static HykProxyXmppResponse parseResponse(String msg)
	{
		int index = msg.indexOf(']');
		String left = msg.substring(index + 1);
		String sessionID = msg.substring(1, index);
		//ret.sessionID = Integer.parseInt(sessionID);
		
		index = left.indexOf(']');
		String seq = left.substring(1, index);
		left = left.substring(index + 1);
		
		return new HykProxyXmppResponse(Integer.parseInt(sessionID), Integer.parseInt(seq), left);
	}
	
}
