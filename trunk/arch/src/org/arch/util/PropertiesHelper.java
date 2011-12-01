/**
 * 
 */
package org.arch.util;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * @author qiyingwang
 *
 */
public class PropertiesHelper
{
	public static int replaceSystemProperties(Properties props)
	{
		int replaceCount = 0;
		Set<Entry<Object, Object>> entries = props.entrySet();
		for(Entry entry:entries)
		{
			String value = (String) entry.getValue();
			int index = value.indexOf("${");
			if(index > 0)
			{
				int end = value.indexOf("}", index);
				if(end > 0)
				{
					String env_key = value.substring(index + 2,end);
					String replace = value.substring(index ,end+1);
					String newvalue = System.getProperty(env_key.trim(), "");
					value = value.replace(replace, newvalue);
					entry.setValue(value);
					replaceCount++;
				}
			}
		}
		return replaceCount;
	}
}
