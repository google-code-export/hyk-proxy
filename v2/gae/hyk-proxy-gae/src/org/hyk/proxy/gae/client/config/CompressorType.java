/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: CompressorType.java 
 *
 * @author yinqiwen [ 2011-11-27 | обнГ09:45:14 ]
 *
 */
package org.hyk.proxy.gae.client.config;

/**
 *
 */
public enum CompressorType
{
	NONE(0), SNAPPY(1), LZF(2);

	int value;

	CompressorType(int i)
	{
		value = i;
	}
	
}
