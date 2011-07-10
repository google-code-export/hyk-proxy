/**
 * 
 */
package com.hyk.rpc.core.remote;

import java.io.IOException;
import java.io.NotSerializableException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.rpc.core.ResponseListener;
import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.message.Message;
import com.hyk.rpc.core.message.MessageFactory;
import com.hyk.rpc.core.session.Session;
import com.hyk.rpc.core.session.SessionManager;
import com.hyk.serializer.Externalizable;
import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;
import com.hyk.util.thread.ThreadLocalUtil;

/**
 * @author qiying.wang
 *
 */
public class RemoteObjectProxy implements InvocationHandler, Externalizable {
	
	protected transient Logger			logger			= LoggerFactory.getLogger(getClass());
	protected long objID = -2;
	protected Address hostAddress;
	protected transient SessionManager sessionManager;
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public Address getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(Address hostAddress) {
		this.hostAddress = hostAddress;
	}

	public long getObjID() {
		return objID;
	}

	public void setObjID(long objID) {
		this.objID = objID;
	}

	public RemoteObjectProxy()
	{
		//
	}
	
    protected void invokeWithResponseListener(Method method, Object[] invokeArgs, ResponseListener listener) throws NotSerializableException, IOException
    {
    	Message msg = MessageFactory.instance.createRequest(objID, method.getName(), invokeArgs);
		msg.setAddress(hostAddress);
		Session session = sessionManager.createClientSession(msg);
		session.setResponseListener(listener);
		session.sendRequest();
    }
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable 
	{
		if(method.getDeclaringClass().equals(Object.class))
		{
			return method.invoke(this, args);
		}
		//int methodID = RemoteUtil.getMethodID(method, proxy);
		if(logger.isDebugEnabled())
		{
			logger.debug("Invoke method:" + method.getName());
		}
		RemoteInvocationResultFuture future = new RemoteInvocationResultFuture();
		invokeWithResponseListener(method, args, future);
		return future.get(sessionManager.getSessionTimeout());
	}

	@Override
	public void readExternal(SerializerInput in) throws IOException
	{
		objID = in.readLong();
		//System.out.println("######" + objID);
		hostAddress = in.readObject(Address.class);
		sessionManager = ThreadLocalUtil.getThreadLocalUtil(SessionManager.class).getThreadLocalObject();
	}

	@Override
	public void writeExternal(SerializerOutput out) throws IOException
	{
		out.writeLong(objID);
		out.writeObject(hostAddress, Address.class);
	}
}
