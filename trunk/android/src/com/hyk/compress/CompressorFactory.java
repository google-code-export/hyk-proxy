/**
 * 
 */
package com.hyk.compress;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.hyk.compress.compressor.Compressor;
import com.hyk.compress.compressor.gz.GZipCompressor;
import com.hyk.compress.compressor.none.NoneCompressor;
import com.hyk.compress.compressor.zip.ZipCompressor;

/**
 * @author Administrator
 * 
 */
public class CompressorFactory
{
	public static class RegistCompressor
	{
		public RegistCompressor(Compressor compressor, int id)
		{
			this.compressor = compressor;
			this.id = id;
		}

		public final Compressor	compressor;
		public final int		id;
	}

	private static Map<String, RegistCompressor>	NAME_TO_REGISTE_COMPRESSOR_TABLE	= new ConcurrentHashMap<String, RegistCompressor>();
	private static Map<Integer, RegistCompressor>	ID_TO_REGISTE_COMPRESSOR_TABLE		= new ConcurrentHashMap<Integer, RegistCompressor>();

	private static AtomicInteger					ID_SEED								= new AtomicInteger(0);

	static
	{
		init();
	}

	private static void init()
	{
		addCompressor(new NoneCompressor());
		addCompressor(new GZipCompressor());
		addCompressor(new ZipCompressor());
	}

	private static void addCompressor(Compressor compressor)
	{
		addCompressor(compressor, ID_SEED.getAndIncrement());
	}
	
	public static Collection<RegistCompressor> getAllRegistCompressors()
	{
		return NAME_TO_REGISTE_COMPRESSOR_TABLE.values();
	}

	private static void addCompressor(Compressor compressor, int id)
	{
		if(NAME_TO_REGISTE_COMPRESSOR_TABLE.containsKey(compressor.getName()))
		{
			throw new IllegalArgumentException("Duplicate compressor name:" + compressor.getName());
		}
		if(ID_TO_REGISTE_COMPRESSOR_TABLE.containsKey(id))
		{
			throw new IllegalArgumentException("Duplicate compressor id:" + id);
		}
		NAME_TO_REGISTE_COMPRESSOR_TABLE.put(compressor.getName(), new RegistCompressor(compressor, id));
		ID_TO_REGISTE_COMPRESSOR_TABLE.put(id, new RegistCompressor(compressor, id));
	}

	public static RegistCompressor getRegistCompressor(String name)
	{
		return NAME_TO_REGISTE_COMPRESSOR_TABLE.get(name);
	}

	public static RegistCompressor getRegistCompressor(int id)
	{
		return ID_TO_REGISTE_COMPRESSOR_TABLE.get(id);
	}

	public static void init(CompressorFactoryOptions options)
	{
		for(RegistCompressor registCompressor : options.getRegistCompressors())
		{
			if(registCompressor.id < 0)
			{
				addCompressor(registCompressor.compressor);
			}
			else
			{
				addCompressor(registCompressor.compressor, registCompressor.id);
			}
		}
	}
}
