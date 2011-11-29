/**
 * 
 */
package org.hyk.proxy.gae.common;

/**
 * @author qiyingwang
 * 
 */
public enum EncryptType
{
	NONE(0), SE1(1);

	int value;

	EncryptType(int v)
	{
		this.value = v;
	}
	public int getValue()
    {
	    return value;
    }
	
	public static EncryptType fromInt(int v)
	{
		if(v > SE1.value) return null;
		return values()[v - 1];
	}
}
