/**
 * 
 */
package com.hyk.rpc.core.message;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;

/**
 * @author qiying.wang
 *
 */
public class Request extends AbstractMessageObject{

	public long getObjID() {
		return objID;
	}



	public String getOperation() {
		return operation;
	}



	public Object[] getArgs() {
		return args;
	}



	long objID;
	String operation;
	Object[] args;
	
	

	@Override
	public MessageType getType() {
		return MessageType.Request;
	}



	@Override
	public void readExternal(SerializerInput in) throws IOException {
		objID = in.readLong();
		operation = in.readString();
		args = in.readObject(Object[].class);
	}



	@Override
	public void writeExternal(SerializerOutput out) throws IOException {
		out.writeLong(objID);
		out.writeString(operation);
		
		out.writeObject(args);
	}

	
	
}
