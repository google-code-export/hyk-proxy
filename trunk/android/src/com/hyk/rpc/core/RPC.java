/**
 * 
 */
package com.hyk.rpc.core;

import java.lang.reflect.Proxy;
import java.util.Properties;

import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.remote.RemoteObjectFactory;
import com.hyk.rpc.core.remote.RemoteObjectProxy;
import com.hyk.rpc.core.service.NameService;
import com.hyk.rpc.core.service.NameServiceImpl;
import com.hyk.rpc.core.session.SessionManager;
import com.hyk.rpc.core.transport.RpcChannel;
import com.hyk.rpc.core.util.ID;

/**
 * @author Administrator
 * 
 */
public class RPC
{
	private RpcChannel			channel;
	private RemoteObjectFactory	remoteObjectFactory;
	private SessionManager		sessionManager;
	private Properties			props;

	public SessionManager getSessionManager()
	{
		return sessionManager;
	}

	private NameService	nameService;

	public RPC(RpcChannel channel) throws RpcException
	{
		this(channel, null);
	}

	public RPC(RpcChannel channel, Properties props) throws RpcException
	{
		try
		{
			this.channel = channel;
			remoteObjectFactory = new RemoteObjectFactory(channel.getRpcChannelAddress());
			sessionManager = new SessionManager(channel, remoteObjectFactory, props);
			nameService = new NameServiceImpl(remoteObjectFactory);
			this.props = props;
			channel.configure(props);
			remoteObjectFactory.configure(props);
		}
		catch(Throwable e)
		{
			throw new RpcException("Init RPC error.", e);
		}
		
	}

	public NameService getLocalNaming()
	{
		return nameService;
	}

	public NameService getRemoteNaming(Address address)
	{
		RemoteObjectProxy proxy = new RemoteObjectProxy();
		proxy.setObjID(ID.NAMING_ID);
		proxy.setHostAddress(address);
		proxy.setSessionManager(sessionManager);
		NameService remoteNaming = (NameService)Proxy.newProxyInstance(NameService.class.getClassLoader(), new Class[] {NameService.class}, proxy);
		return remoteNaming;
	}

	public <T> T getRemoteService(Class<T> clazz, String name, Address address)
	{
		NameService remoteNaming = getRemoteNaming(address);
		return (T)remoteNaming.lookup(name);
	}
	
	public void shutdown()
	{
		channel.close();
	}
	
	public void destroy(Object obj)
	{
		remoteObjectFactory.remove(obj);
	}
	
	public Object exportRemoteObject(Object obj)
	{
		return remoteObjectFactory.publish(obj);
	}
	
	public Object exportRemoteObject(Object obj, long objid)
	{
		return remoteObjectFactory.publish(obj, objid);
	}
	
	public static long getRemoteObjectId(Object obj)
	{
		return RemoteObjectFactory.getRemoteObjectId(obj);
	}
	
	public static Address getRemoteObjectAddress(Object obj)
	{
		return RemoteObjectFactory.getRemoteObjectAddress(obj);
	}
	
	public void close()
	{
		channel.close();
	}
}
