/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: DefaultCompressPreference.java 
 *
 * @author yinqiwen [ 2010-3-28 | 09:35:02 PM ]
 *
 */
package com.hyk.compress.preference;

import com.hyk.compress.compressor.Compressor;

/**
 *
 */
public class DefaultCompressPreference implements CompressPreference
{
	private static Compressor compressor;
	
	public static void init(Compressor compressor)
	{
		DefaultCompressPreference.compressor = compressor;
	}
	
	@Override
	public Compressor getCompressor()
	{
		return compressor;
	}


}
