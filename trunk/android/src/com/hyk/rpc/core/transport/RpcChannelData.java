/**
 * 
 */
package com.hyk.rpc.core.transport;

import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.rpc.core.address.Address;

/**
 * @author Administrator
 *
 */
public class RpcChannelData {

	public RpcChannelData(ChannelDataBuffer data, Address address) {
		this.content = data;
		this.address = address;
	}
	public final ChannelDataBuffer content;
	public final Address address;
}
