package org.arch.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

public class PropertiesHelperTest
{

	@Test
	public void testReplaceSystemProperties() throws IOException
	{
		Properties props = new Properties();
		props.load(PropertiesHelperTest.class.getResourceAsStream("logging properties"));
		System.setProperty("HYK_PROXY_HOME", "#####");
		int ret = PropertiesHelper.replaceSystemProperties(props);
		assertEquals("#####/log/hyk-proxy.log", props.getProperty("java.util.logging.FileHandler.pattern"));
		assertEquals(1, ret);
	}

}
