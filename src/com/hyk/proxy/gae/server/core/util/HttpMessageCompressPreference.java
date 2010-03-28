/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpMessageCompressPreference.java 
 *
 * @author yinqiwen [ 2010-3-28 | ÏÂÎç09:47:48 ]
 *
 */
package com.hyk.proxy.gae.server.core.util;

import com.hyk.compress.CompressPreference;
import com.hyk.compress.Compressor;
import com.hyk.util.thread.ThreadLocalUtil;

/**
 *
 */
public class HttpMessageCompressPreference implements CompressPreference
{
	private static Compressor compressor;
	private static int trigger;
	
	public static void init(Compressor compressor, int trigger)
	{
		HttpMessageCompressPreference.compressor = compressor;
		HttpMessageCompressPreference.trigger = trigger;
	}
	
	@Override
	public Compressor getCompressor()
	{
		String contentType = ThreadLocalUtil.getThreadLocalUtil(String.class).getThreadLocalObject();
		if(null != contentType)
		{
			
		}
		return compressor;
	}

	@Override
	public int getTrigger()
	{
		return trigger;
	}

}
