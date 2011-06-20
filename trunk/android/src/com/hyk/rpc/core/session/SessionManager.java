/**
 * 
 */
package com.hyk.rpc.core.session;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.rpc.core.constant.RpcConstants;
import com.hyk.rpc.core.message.Message;
import com.hyk.rpc.core.message.MessageID;
import com.hyk.rpc.core.remote.RemoteObjectFactory;
import com.hyk.rpc.core.transport.MessageListener;
import com.hyk.rpc.core.transport.RpcChannel;
import com.hyk.timer.Timer;

/**
 * @author Administrator
 * 
 */
public class SessionManager implements MessageListener
{
	protected Logger				logger				= LoggerFactory.getLogger(getClass());

	private Map<Long, Session>		clientSessionMap	= new ConcurrentHashMap<Long, Session>();
	private Map<MessageID, Session>	serverSessionMap	= new ConcurrentHashMap<MessageID, Session>();

	private RpcChannel				channel;
	private RemoteObjectFactory		remoteObjectFactory;

	private int						sessionTimeout		= 1200;

	Timer							timer				= null;

	public SessionManager(RpcChannel channel, RemoteObjectFactory remoteObjectFactory, Properties initProps) throws Exception
	{
		this.channel = channel;
		this.remoteObjectFactory = remoteObjectFactory;
		channel.setSessionManager(this);
		configure(initProps);
	}
	
	public void configure(Properties initProps) throws Exception
	{
		String timerClass = null;
		if(null != initProps)
		{
			timerClass = initProps.getProperty(RpcConstants.TIMER_CLASS);
			
			String timeout = initProps.getProperty(RpcConstants.SESSIN_TIMEOUT);
			if(null != timeout)
			{
				sessionTimeout = Integer.parseInt(timeout.trim());
			}
		}
		if(null == timerClass)
		{
			timerClass = RpcConstants.DEFAULT_TIMER;
		}
		{
			timer = (Timer)Class.forName(timerClass).newInstance();
		}
	}

	private Session createSession(Message request, int type)
	{
		Session session = new Session(this, request, channel, remoteObjectFactory);
		switch(type)
		{
			case Session.CLIENT:
			{
				clientSessionMap.put(request.getSessionID(), session);
				break;
			}
			case Session.SERVER:
			{
				serverSessionMap.put(request.getId(), session);
				break;
			}
			default:
				break;
		}
		return session;
	}

	public Session removeClientSession(long sessionID)
	{
		return clientSessionMap.remove(sessionID);
	}

	public Session getClientSession(long sessionID)
	{
		return clientSessionMap.get(sessionID);
	}

	public Session getServerSession(MessageID id)
	{
		return serverSessionMap.get(id);
	}

	public void removeServerSession(Session session)
	{
		Message request = session.request;
		serverSessionMap.remove(request.getId());
	}

	public Session createClientSession(Message request)
	{
		return createSession(request, Session.CLIENT);
	}

	public Session createServerSession(Message request)
	{
		return createSession(request, Session.SERVER);
	}

	public int getSessionTimeout()
	{
		return sessionTimeout;
	}
	
	public void setSessionTimeout(int value)
	{
		sessionTimeout = value;
	}

	@Override
	public void onMessage(Message msg)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Handle message:" + msg.getType());
		}
		switch(msg.getType())
		{
			case Request:
			{
				Session session = null;
				if(!serverSessionMap.containsKey(msg.getId()))
				{
					session = createServerSession(msg);
				}
				else
				{
					session = getServerSession(msg.getId());
					if(logger.isDebugEnabled())
					{
						logger.debug("Duplicate request!");
					}
				}
				session.processRequest();
				break;
			}
			case Response:
			{
				Session session = removeClientSession(msg.getSessionID());
				if(null != session)
				{
					session.processResponse(msg);
				}
				else
				{
					if(logger.isDebugEnabled())
					{
						logger.debug("Duplicate response!");
					}
				}
				break;
			}
			default:
				break;
		}
	}
}
