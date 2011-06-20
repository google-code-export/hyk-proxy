/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: CompressPreference.java 
 *
 * @author yinqiwen [ 2010-3-28 | 09:32:51 PM ]
 *
 */
package com.hyk.compress.preference;

import com.hyk.compress.compressor.Compressor;

/**
 *
 */
public interface CompressPreference
{
	Compressor getCompressor();
	//int getTrigger();
}
