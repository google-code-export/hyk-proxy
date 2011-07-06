/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ExtensionsLauncher.java 
 *
 * @author qiying.wang [ Apr 20, 2010 | 1:45:12 PM ]
 *
 */
package com.hyk.proxy.common;

import com.hyk.compress.CompressorFactory;
import com.hyk.compress.CompressorFactoryOptions;
import com.hyk.proxy.common.rpc.extension.compress.lzf.LZFCompressor;
import com.hyk.proxy.common.secure.SecurityServiceFactory;
import com.hyk.proxy.common.secure.SimpleSecurityService;

/**
 *
 */
public class ExtensionsLauncher
{
	private static void loadLZFCompressExtension()
	{
		CompressorFactoryOptions options = new CompressorFactoryOptions();
		options.addCompressor(new LZFCompressor());
		CompressorFactory.init(options);
	}
	
	private static void loadHttpSecurityExtension()
	{
		SecurityServiceFactory.registerSecurityService(new SimpleSecurityService());
	}
	
	public static void init()
	{
		loadLZFCompressExtension();
		loadHttpSecurityExtension();
	}
}
