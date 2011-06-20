/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: CompressorFactoryOptions.java 
 *
 * @author qiying.wang [ Apr 20, 2010 | 10:46:02 AM ]
 *
 */
package com.hyk.compress;

import java.util.ArrayList;
import java.util.List;

import com.hyk.compress.CompressorFactory.RegistCompressor;
import com.hyk.compress.compressor.Compressor;

/**
 *
 */
public class CompressorFactoryOptions
{
	private List<RegistCompressor> registCompressors = new ArrayList<RegistCompressor>();
	
	public void addCompressor(Compressor compressor, int id)
	{
		registCompressors.add(new RegistCompressor(compressor, id));
	}
	
	public void addCompressor(Compressor compressor)
	{
		registCompressors.add(new RegistCompressor(compressor, -1));
	}
	
	public List<RegistCompressor> getRegistCompressors()
	{
		return registCompressors;
	}
}
