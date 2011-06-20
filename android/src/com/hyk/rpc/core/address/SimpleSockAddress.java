/**
 * 
 */
package com.hyk.rpc.core.address;

/**
 * @author Administrator
 * 
 */
public class SimpleSockAddress implements Address {

	@Override
	public String toString()
	{
		return "SimpleSockAddress [host=" + host + ", port=" + port + "]";
	}

	private String host;
	private int port;

	public SimpleSockAddress() {
	}

	public SimpleSockAddress(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof SimpleSockAddress) {
			SimpleSockAddress anotherAddr = (SimpleSockAddress) anObject;

			return anotherAddr.port == port
					&& anotherAddr.host.endsWith(host);
		}
		return false;
	}

	public int hashCode() {
		return port + host.hashCode();
	}

	@Override
	public String toPrintableString()
	{
		return "[" + host + ":" + port + "]";
	}
}
