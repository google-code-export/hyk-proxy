/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010, BigBand Networks Inc. All rights reserved.
 *
 * Description: Externalizable.java 
 *
 * @author qiying.wang [ Jan 22, 2010 | 5:11:47 PM ]
 *
 */
package com.hyk.serializer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 *
 */
public interface Externalizable extends Serializable
{
	public void readExternal(SerializerInput in)throws IOException;
	public void writeExternal(SerializerOutput out) throws IOException;
}
