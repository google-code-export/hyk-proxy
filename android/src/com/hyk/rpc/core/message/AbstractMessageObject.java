/**
 * 
 */
package com.hyk.rpc.core.message;

import com.hyk.serializer.Externalizable;





/**
 * @author qiying.wang
 *
 */
public abstract class AbstractMessageObject implements Externalizable{

	public abstract MessageType getType();
	
}
