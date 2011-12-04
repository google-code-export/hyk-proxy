/**
 * 
 */
package org.hyk.proxy.gae.common;

/**
 * @author qiyingwang
 *
 */
public enum CompressorType
{
	NONE(0), SNAPPY(1), LZF(2), FASTLZ(3), QUICKLZ(4);
	
	int value;
	CompressorType(int v)
	{
		this.value = v;
	}
	public int getValue()
    {
	    return value;
    }
	
	public static CompressorType fromInt(int v)
	{
		if(v > QUICKLZ.value) return null;
		return values()[v];
	}
	
}
