/**
 * 
 */
package org.hyk.proxy.gae.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.arch.buffer.Buffer;
import org.arch.compress.fastlz.JFastLZ;
import org.arch.compress.jsnappy.SnappyBuffer;
import org.arch.compress.jsnappy.SnappyCompressor;
import org.arch.compress.jsnappy.SnappyDecompressor;
import org.arch.compress.lzf.LZFDecoder;
import org.arch.compress.lzf.LZFEncoder;
import org.arch.encrypt.SimpleEncrypt;
import org.arch.event.Event;
import org.arch.event.EventDispatcher;
import org.arch.event.EventSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 * 
 */
public class GAEEventHelper
{
	protected static Logger logger = LoggerFactory.getLogger(GAEEventHelper.class);
	public static Event parseEvent(Buffer buffer) throws Exception
	{
		EventHeaderTags tags = new EventHeaderTags();
		return parseEvent(buffer ,tags);
	}

	public static Event parseEvent(Buffer buffer, EventHeaderTags tags) throws Exception
	{
		//EventHeaderTags tags = new EventHeaderTags();
		if (!EventHeaderTags.readHeaderTags(buffer, tags))
		{
			return null;
		}
		switch (tags.encrypter)
		{
			case SE1:
			{
				SimpleEncrypt se1 = new SimpleEncrypt();
				se1.decrypt(buffer);
				break;
			}
			default:
			{
				break;
			}
		}
		switch (tags.compressor)
		{
			case FASTLZ:
			{
				byte[] raw = buffer.getRawBuffer();
				int len = buffer.readableBytes()*10;
				try
                {
					JFastLZ fastlz = new JFastLZ();
					byte[] newbuf = new byte[len];
					int decompressed = fastlz.fastlzDecompress(raw, buffer.getReadIndex(), buffer.readableBytes(), newbuf, 0, len);
					buffer = Buffer.wrapReadableContent(newbuf, 0, decompressed);
                }
                catch (Exception e)
                {
                	logger.error("Failed to uncompress by SNAPPY.", e);
	                return null;
                }
				break;
			}
			case SNAPPY:
			{
				byte[] raw = buffer.getRawBuffer();
				try
                {
					SnappyBuffer newbuf = SnappyDecompressor.decompress(raw,
					        buffer.getReadIndex(), buffer.readableBytes());
					buffer = Buffer.wrapReadableContent(newbuf.getData(), 0, newbuf.getLength());
                }
                catch (Exception e)
                {
                	logger.error("Failed to uncompress by SNAPPY.", e);
	                return null;
                }
				
				break;
			}
			case LZF:
			{
				byte[] raw = buffer.getRawBuffer();
				try
                {
	                byte[] newbuf = LZFDecoder.decode(raw, buffer.getReadIndex(), buffer.readableBytes());
	                buffer = Buffer.wrapReadableContent(newbuf);
                }
                catch (Exception e)
                {
                	logger.error("Failed to uncompress by LZF.", e);
	                return null;
                }
				break;
			}
			default:
			{
				break;
			}
		}
		return EventDispatcher.getSingletonInstance().parse(buffer);
	}

	public static Buffer encodeEvent(EventHeaderTags tags, Event event)
	{
		Buffer buf = new Buffer(256);
		tags.encode(buf);
		Buffer content = new Buffer(256);
		event.encode(content);
		switch (tags.compressor)
		{
			case FASTLZ:
			{
				byte[] raw = content.getRawBuffer();
				byte[] newbuf = new byte[raw.length];
				JFastLZ fastlz = new JFastLZ();
				int afterCompress;
                try
                {
	                afterCompress = fastlz.fastlzCompress(raw, content.getReadIndex(), content.readableBytes(), newbuf, 0, newbuf.length);
                }
                catch (IOException e)
                {
                	logger.error("Failed to compress by FastLZ.", e);
	                return null;
                }
				content = Buffer.wrapReadableContent(newbuf, 0, afterCompress);
				break;
			}
			case SNAPPY:
			{	
				byte[] raw = content.getRawBuffer();
				
				SnappyBuffer newbuf = SnappyCompressor.compress(raw,
				        content.getReadIndex(), content.readableBytes());
				content = Buffer.wrapReadableContent(newbuf.getData(), 0, newbuf.getLength());
				break;
			}
			case LZF:
			{
				byte[] raw = content.getRawBuffer();
				byte[] newbuf;
				try
				{
					newbuf = LZFEncoder.encode(raw, content.readableBytes());
					content = Buffer.wrapReadableContent(newbuf, 0,
					        newbuf.length);
				}
				catch (Exception e)
				{
					logger.error("Failed to compress by LZF.", e);
	                return null;
				}
				break;
			}
			default:
			{
				break;
			}
		}
		switch (tags.encrypter)
		{
			case SE1:
			{
				SimpleEncrypt se1 = new SimpleEncrypt();
				se1.encrypt(content);
				break;
			}
			default:
			{
				break;
			}
		}

		buf.write(content, content.readableBytes());
		return buf;
	}
	
	private static Map<Integer, ArrayList<EventSegment>> sessionBufferTable = new HashMap<Integer, ArrayList<EventSegment>>();
	
	public static Buffer mergeEventSegment(EventSegment segment)
	{
		ArrayList<EventSegment> segmentList = sessionBufferTable.get(segment.getHash());
		if(null == segmentList)
		{
			segmentList = new ArrayList<EventSegment>();
			sessionBufferTable.put(segment.getHash(), segmentList);
		}
		segmentList.add(segment);
		if(segmentList.size() == segment.total)
		{
			Collections.sort(segmentList);
			Buffer content = new Buffer(4096);
			for(EventSegment seg:segmentList)
			{
				content.write(seg.content, seg.content.readableBytes());
			}
			sessionBufferTable.remove(segment.getHash());
			return content;
		}
		return null;
	}
}
