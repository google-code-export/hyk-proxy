/**
 * 
 */
package com.hyk.rpc.core.transport;

import com.hyk.rpc.core.message.Message;


/**
 * @author qiying.wang
 *
 */
public interface MessageListener {

	void onMessage(Message msg);
}
