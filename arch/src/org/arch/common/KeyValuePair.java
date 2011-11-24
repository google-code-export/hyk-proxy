/**
 * 
 */
package org.arch.common;

/**
 * @author qiyingwang
 *
 */
public class KeyValuePair<K, V>
{
	private K name;
	private V value;
	
	public KeyValuePair(K name, V v)
	{
		this.name = name;
		this.value = v;
	}
	
	public K getName()
	{
		return name;
	}
	
	public V getValue()
	{
		return value;
	}
}
