/**
 * 
 */
package org.arch.event;

/**
 * @author qiyingwang
 * 
 */
public class TypeVersion
{
	public int type;
	public int version;

	@Override
	public boolean equals(Object anObject)
	{
		if (this == anObject)
		{
			return true;
		}
		if(null == anObject)
		{
			return false;
		}
		if (anObject instanceof TypeVersion)
		{
			TypeVersion anotherObj = (TypeVersion) anObject;
			if(type == anotherObj.type && version == anotherObj.version)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return type + version;
	}
}
