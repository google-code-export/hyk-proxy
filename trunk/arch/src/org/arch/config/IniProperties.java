/**
 * 
 */
package org.arch.config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author qiyingwang
 * 
 */
public class IniProperties
{
	private Map<String, Properties> propsTable = new HashMap<String, Properties>();

	public void load(String file) throws IOException
	{
		FileInputStream fis = new FileInputStream(file);
		load(fis);
		fis.close();
	}
	
	public synchronized void load(InputStream in) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		String currentTag = "";
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			if (line.equals("") || line.startsWith("#"))
			{
				continue;
			}

			if (line.startsWith("[") && line.endsWith("]"))
			{
				currentTag = line.substring(1, line.length() - 1);
				continue;
			}
			String[] splits = line.split("=");
			if (splits.length == 2)
			{
				String key = splits[0];
				String value = splits[1];
				Properties props = propsTable.get(currentTag);
				if(null == props)
				{
					props = new Properties();
					propsTable.put(currentTag, props);
				}
				props.put(key, value);
			}
		}
	}
	
	public Properties getProperties(String tag)
	{
		return propsTable.get(tag);
	}
}
