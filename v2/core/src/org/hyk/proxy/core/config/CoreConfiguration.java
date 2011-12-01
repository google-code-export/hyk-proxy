/**
 * 
 */
package org.hyk.proxy.core.config;


/**
 * @author qiyingwang
 *
 */
public interface CoreConfiguration
{
	public String getProxyEventHandler();

	public SimpleSocketAddress getLocalProxyServerAddress();

	public int getThreadPoolSize();
}
