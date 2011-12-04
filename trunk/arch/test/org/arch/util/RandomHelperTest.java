package org.arch.util;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;


public class RandomHelperTest
{
	@Test
	public void testGeneration()
	{
		String r1 = RandomHelper.generateRandomString(10);
		String r2 = RandomHelper.generateRandomString(10);
		System.out.println(r1);
		System.out.println(r2);
		Assert.assertNotSame(r1, r2);
	}
}
