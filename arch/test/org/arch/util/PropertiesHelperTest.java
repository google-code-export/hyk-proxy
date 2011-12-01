package org.arch.util;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

public class PropertiesHelperTest
{

	@Test
	public void testReplaceSystemProperties()
	{
		Properties props = new Properties();
		System.setProperty("HYK_HOME", "FGHFH");
		props.put("key", "XXXX${HYK_HOME}/yyyy");
		int ret = PropertiesHelper.replaceSystemProperties(props);
		assertEquals("XXXXFGHFH/yyyy", props.getProperty("key"));
		assertEquals(1, ret);
	}

}
