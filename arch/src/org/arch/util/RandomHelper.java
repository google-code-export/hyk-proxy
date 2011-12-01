/**
 * This file is part of the hyk-util project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RandomUtil.java 
 *
 * @author qiying.wang [ Apr 8, 2010 | 3:27:04 PM ]
 *
 */
package org.arch.util;

import java.util.Random;

/**
 *
 */
public class RandomHelper
{
	static final char[] SEED = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz~!@#$%^&*()-+<>"
	        .toCharArray();
	static Random rnd = new Random();

	public static String generateRandomString(int len)
	{
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(SEED[rnd.nextInt(SEED.length)]);
		return sb.toString();
	}
}
