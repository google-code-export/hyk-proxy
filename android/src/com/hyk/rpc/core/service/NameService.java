/**
 * 
 */
package com.hyk.rpc.core.service;

import com.hyk.rpc.core.annotation.Remote;

/**
 * @author qiying.wang
 *
 */
@Remote
public  interface NameService {
	public Object lookup(String name);
	public boolean bind(String name, Object obj);
	public void unbind(String name);
}
