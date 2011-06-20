/**
 * 
 */
package com.hyk.rpc.core.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.constant.RpcConstants;
import com.hyk.rpc.core.util.RemoteUtil;

/**
 * @author qiying.wang
 * 
 */
public class RemoteObjectFactory
{
	protected Logger			logger					= LoggerFactory.getLogger(getClass());
	private Address				localAddress;
	private Map<Long, RemoteObjectReference>	remoteRawObjectTable	= new ConcurrentHashMap<Long, RemoteObjectReference>();

	private RemoteObjectIdGenerator idgen = RemoteObjectIdGenerator.defaultGenerator;
	
	private RemoteObjectStorage remoteObjectStorage;
	
	public RemoteObjectFactory(Address localAddress)
	{
		this.localAddress = localAddress;
	}
	
	public void configure(Properties initProps) throws Exception
	{
		String genClassName = null != initProps ?initProps.getProperty(RpcConstants.REMOTE_OBJECTID_GEN):null;
		if(null != genClassName)
		{
			idgen = (RemoteObjectIdGenerator)Class.forName(genClassName).newInstance();
		}
		
		String storeageClassName = null != initProps ?initProps.getProperty(RpcConstants.REMOTE_OBJECT_STORAGE):null;
		if(null != storeageClassName)
		{
			remoteObjectStorage = (RemoteObjectStorage)Class.forName(storeageClassName).newInstance();
		}
		loadAllRemoteObjects();
	}
	
	private void saveRemoteObject(RemoteObjectProxy remoteObjectProxy, Object rawObj)
	{
		RemoteObjectReference ref = RemoteObjectReference.refernce(remoteObjectProxy.getObjID(), rawObj);
		remoteRawObjectTable.put(remoteObjectProxy.getObjID(), ref);
		if(null != remoteObjectStorage)
		{
			remoteObjectStorage.store(ref);
		}
	}
	
	private void loadAllRemoteObjects()
	{
		if(null != remoteObjectStorage)
		{
			List<RemoteObjectReference> refs = remoteObjectStorage.loadAll();
			
			if(null != refs)
			{
				for(RemoteObjectReference ref:refs)
				{
					remoteRawObjectTable.put(ref.getObjID(), ref);
				}
			}
		}
	}

	public boolean remove(Object obj)
	{
		if(Proxy.isProxyClass(obj.getClass()))
		{
			InvocationHandler handler = Proxy.getInvocationHandler(obj);
			if(handler instanceof RemoteObjectProxy)
			{
				long id = ((RemoteObjectProxy)handler).getObjID();
				remoteRawObjectTable.remove(id);
				return true;
			}
		}
		return false;
	}
	
	public Object publish(Object obj)
	{
		if(Proxy.isProxyClass(obj.getClass()))
		{
			if(Proxy.getInvocationHandler(obj) instanceof  RemoteObjectProxy)
			{
				return obj;
			}
		}
		return publish(obj, idgen.generateRemoteObjectID());
	}

	
	public Object publish(Object obj, long id)
	{
		if(Proxy.isProxyClass(obj.getClass()))
		{
			if(Proxy.getInvocationHandler(obj) instanceof  RemoteObjectProxy)
			{
				RemoteObjectProxy setting = (RemoteObjectProxy) Proxy.getInvocationHandler(obj);
				setting.setObjID(id);
				return obj;
			}
		}
		RemoteObjectProxy remoteObjectProxy = new RemoteObjectProxy();
		remoteObjectProxy.setHostAddress(localAddress);
		Class[] remoteInterfaces =  RemoteUtil.getRemoteInterfaces(obj.getClass());
		Object proxy = Proxy.newProxyInstance(obj.getClass().getClassLoader(),remoteInterfaces, remoteObjectProxy);
		remoteObjectProxy.setObjID(id);
		saveRemoteObject(remoteObjectProxy, obj);
		return proxy;
	}

	public Object getRawObject(long id)
	{
		RemoteObjectReference ref = remoteRawObjectTable.get(id);
		if(null == ref)
		{
			return null;
		}
		return ref.getImpl();
	}
	
	public static long getRemoteObjectId(Object obj) 
	{
		if(Proxy.isProxyClass(obj.getClass()))
		{
			InvocationHandler handler = Proxy.getInvocationHandler(obj);
			if(handler instanceof RemoteObjectProxy)
			{
				long id = ((RemoteObjectProxy)handler).getObjID();
				return id;
			}
		}
		return 0;
	}
	
	public static Address getRemoteObjectAddress(Object obj) 
	{
		if(Proxy.isProxyClass(obj.getClass()))
		{
			InvocationHandler handler = Proxy.getInvocationHandler(obj);
			if(handler instanceof RemoteObjectProxy)
			{
				return ((RemoteObjectProxy)handler).getHostAddress();
			}
		}
		return null;
	}
	
	public Object getRawObject(Object obj)
	{
		if(Proxy.isProxyClass(obj.getClass()))
		{
			InvocationHandler handler = Proxy.getInvocationHandler(obj);
			if(handler instanceof RemoteObjectProxy)
			{
				long id = ((RemoteObjectProxy)handler).getObjID();
				return remoteRawObjectTable.get(id).getImpl();
			}
		}
		return null;
	}

}
