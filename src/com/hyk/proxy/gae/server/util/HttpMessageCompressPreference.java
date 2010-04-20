/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpMessageCompressPreference.java 
 *
 * @author yinqiwen [ 2010-3-28 | 09:47:48 pm ]
 *
 */
package com.hyk.proxy.gae.server.util;

import java.util.List;

import com.hyk.compress.CompressorFactory;
import com.hyk.compress.compressor.Compressor;
import com.hyk.compress.compressor.none.NoneCompressor;
import com.hyk.compress.preference.CompressPreference;
import com.hyk.util.thread.ThreadLocalUtil;

/**
 *
 */
public class HttpMessageCompressPreference implements CompressPreference
{
	private static Compressor compressor;
	private static int trigger;
	private static List<String> ignorePatterns;
	
	public static void init(Compressor compressor, int trigger, List<String> ignorePatterns)
	{
		HttpMessageCompressPreference.compressor = compressor;
		HttpMessageCompressPreference.trigger = trigger;
		HttpMessageCompressPreference.ignorePatterns = ignorePatterns;
	}
	
	@Override
	public Compressor getCompressor()
	{
		String contentType = ThreadLocalUtil.getThreadLocalUtil(String.class).getThreadLocalObject();
		if(!compressor.getName().equals(NoneCompressor.NAME) && null != contentType)
		{
			if(null != ignorePatterns)
			{
				for(String p:ignorePatterns)
				{
					if(contentType.toLowerCase().indexOf(p) != -1)
					{
						return CompressorFactory.getRegistCompressor(NoneCompressor.NAME).compressor;
					}
				}
			}
		}
		return compressor;
	}

	@Override
	public int getTrigger()
	{
		return trigger;
	}

}
