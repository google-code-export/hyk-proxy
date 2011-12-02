/**
 * 
 */
package org.hyk.proxy.gae.client.connection;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.arch.buffer.Buffer;
import org.arch.common.Pair;
import org.arch.event.Event;
import org.arch.event.EventHandler;
import org.arch.event.EventHeader;
import org.arch.event.EventSegment;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration;
import org.hyk.proxy.gae.client.config.GAEClientConfiguration.GAEServerAuth;
import org.hyk.proxy.gae.client.handler.ProxySession;
import org.hyk.proxy.gae.client.handler.ProxySessionManager;
import org.hyk.proxy.gae.common.CompressorType;
import org.hyk.proxy.gae.common.EncryptType;
import org.hyk.proxy.gae.common.EventHeaderTags;
import org.hyk.proxy.gae.common.GAEEventHelper;
import org.hyk.proxy.gae.common.event.AuthRequestEvent;
import org.hyk.proxy.gae.common.event.AuthResponseEvent;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 * 
 */
public abstract class ProxyConnection
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected static GAEClientConfiguration cfg = GAEClientConfiguration
	        .getInstance();
	private LinkedList<Event> queuedEvents = new LinkedList<Event>();
	protected GAEServerAuth auth = null;
	private String authToken = null;
	private Object authTokenLock = new Object();
	private Set<Integer> relevantSessions = new HashSet<Integer>();
	private EventHandler outSessionHandler = null;

	protected ProxyConnection(GAEServerAuth auth)
	{
		this.auth = auth;
	}

	protected abstract boolean doSend(Buffer msgbuffer);

	protected abstract int getMaxDataPackageSize();

	protected void doClose()
	{

	}

	public abstract boolean isReady();

	private void closeRelevantSessions()
	{
		for (Integer sessionID : relevantSessions)
		{
			ProxySession session = ProxySessionManager.getInstance()
			        .getProxySession(sessionID);
			if (null != session)
			{
				session.close();
			}
		}
		relevantSessions.clear();
	}

	public void close()
	{
		doClose();
		closeRelevantSessions();
	}

	public boolean auth()
	{
		if (null != authToken)
		{
			return true;
		}
		AuthRequestEvent event = new AuthRequestEvent();
		event.appid = auth.appid;
		event.user = auth.user;
		event.passwd = auth.passwd;
		if (!send(event))
		{
			return false;
		}
		synchronized (authTokenLock)
		{
			try
			{
				authTokenLock.wait(60 * 1000); // 1min
			}
			catch (InterruptedException e)
			{
				return false;
			}
		}
		return authToken != null && !authToken.isEmpty();
	}

	public String getAuthToken()
	{
		return authToken;
	}

	private void setAuthToken(AuthResponseEvent ev)
	{
		synchronized (authTokenLock)
		{
			setAuthToken(ev.token);
			authTokenLock.notify();
		}
		if (null != ev.error)
		{
			logger.error("Failed to auth appid:" + ev.appid + " fore reason:"
			        + ev.error);
		}
	}

	public void setAuthToken(String token)
	{
		authToken = token;
	}

	public boolean send(Event event, EventHandler handler)
	{
		outSessionHandler = handler;
		return send(event);
	}

	public boolean send(Event event)
	{
		if (!isReady())
		{
			queuedEvents.add(event);
			return true;
		}

		EventHeaderTags tags = new EventHeaderTags();
		tags.compressor = cfg.getCompressor();
		tags.encrypter = cfg.getEncrypter();
		tags.token = authToken;
		Pair<Channel, Integer> attach = (Pair<Channel, Integer>) event
		        .getAttachment();
		event.setHash(attach.second);
		relevantSessions.add(attach.second);
		Buffer msgbuffer = GAEEventHelper.encodeEvent(tags, event);
		if (msgbuffer.readableBytes() > getMaxDataPackageSize())
		{
			Buffer[] segments = GAEEventHelper.splitEventBuffer(msgbuffer,
			        event.getHash(), getMaxDataPackageSize(), tags);
			for (int i = 0; i < segments.length; i++)
			{
				doSend(segments[i]);
			}
			return true;
		}
		else
		{
			return doSend(msgbuffer);
		}
	}

	protected void doRecv(Buffer content)
	{
		Event ev = null;
		try
		{
			ev = GAEEventHelper.parseEvent(content);
		}
		catch (Exception e)
		{
			logger.error("Failed to parse event.");
			return;
		}
		relevantSessions.remove(ev.getHash());
		if (ev instanceof AuthResponseEvent)
		{
			setAuthToken((AuthResponseEvent) ev);
			return;
		}
		else if (ev instanceof EventSegment)
		{
			EventSegment segment = (EventSegment) ev;
			Buffer evntContent = GAEEventHelper.mergeEventSegment(segment);
			if (null != evntContent)
			{
				doRecv(evntContent);
			}
			return;
		}
		ProxySession session = ProxySessionManager.getInstance()
		        .getProxySession(ev.getHash());
		if (null != session)
		{
			session.handleResponse(ev);
		}
		else
		{
			if (null != outSessionHandler)
			{
				EventHeader header = new EventHeader();
				header.hash = ev.getHash();
				// header.type = Event.getTypeVersion(ev.getClass())
				outSessionHandler.onEvent(header, ev);
			}
		}
		if (!queuedEvents.isEmpty())
		{
			if (isReady())
			{
				Event qe = queuedEvents.removeFirst();
				send(qe);
			}
		}
	}
}
