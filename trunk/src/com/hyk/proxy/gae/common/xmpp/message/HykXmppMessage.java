/**
 * 
 */
package com.hyk.proxy.gae.common.xmpp.message;

import java.nio.ByteBuffer;

/**
 * @author qiying.wang
 *
 */
public abstract class HykXmppMessage {
	private static final String MAGIC_STR = "#$@123Vba*%$A";
	
	
	protected int type;
	
	protected abstract void encode(ByteBuffer buffer);
	protected abstract void decode(ByteBuffer buffer);
	
}
