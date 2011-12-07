/**
 * 
 */
package org.hyk.proxy.gae.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.arch.buffer.Buffer;
import org.arch.compress.fastlz.JFastLZ;
import org.arch.compress.jsnappy.SnappyBuffer;
import org.arch.compress.jsnappy.SnappyCompressor;
import org.arch.compress.jsnappy.SnappyDecompressor;
import org.arch.compress.lzf.LZFDecoder;
import org.arch.compress.lzf.LZFEncoder;
import org.arch.compress.quicklz.QuickLZ;
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
	protected static Logger logger = LoggerFactory
	        .getLogger(GAEEventHelper.class);

	public static Event parseEvent(Buffer buffer) throws Exception
	{
		EventHeaderTags tags = new EventHeaderTags();
		return parseEvent(buffer, tags);
	}

	public static Event parseEvent(Buffer buffer, EventHeaderTags tags)
	        throws Exception
	{
		// EventHeaderTags tags = new EventHeaderTags();
		if (!EventHeaderTags.readHeaderTags(buffer, tags))
		{
			logger.error("Failed to read event header tags.");
			return null;
		}
		return EventDispatcher.getSingletonInstance().parse(buffer);
	}

	public static Buffer[] splitEventBuffer(Buffer msgbuffer, int hash,
	        int splitsize, EventHeaderTags tags)
	{
		if (msgbuffer.readableBytes() > splitsize)
		{
			int total = msgbuffer.readableBytes() / splitsize;
			if (msgbuffer.readableBytes() % splitsize != 0)
			{
				total++;
			}
			Buffer[] bufs = new Buffer[total];
			int i = 0;
			while (msgbuffer.readable())
			{
				EventSegment segment = new EventSegment();
				segment.setHash(hash);
				segment.sequence = i;
				segment.total = total;
				segment.content = new Buffer(splitsize);
				segment.content.write(msgbuffer, splitsize);
				i++;
				Buffer buf = GAEEventHelper.encodeEvent(tags, segment);
				segment.encode(buf);
				bufs[i] = buf;
			}
			return bufs;
		}
		else
		{
			return new Buffer[] { msgbuffer };
		}
	}

	public static Buffer encodeEvent(EventHeaderTags tags, Event event)
	{
		Buffer buf = new Buffer(256);
		tags.encode(buf);
		Buffer content = new Buffer(256);
		event.encode(content);
		buf.write(content, content.readableBytes());
		return buf;
	}

	private static Map<Integer, ArrayList<EventSegment>> sessionBufferTable = new HashMap<Integer, ArrayList<EventSegment>>();

	public static void releaseSessionBuffer(Integer sessionID)
	{
		sessionBufferTable.remove(sessionID);
	}

	public static Buffer mergeEventSegment(EventSegment segment, CacheService cache)
	{
		ArrayList<EventSegment> segmentList = sessionBufferTable.get(segment
		        .getHash());
		if (null == segmentList)
		{
			segmentList = new ArrayList<EventSegment>();
			sessionBufferTable.put(segment.getHash(), segmentList);
		}
		segmentList.add(segment);
		if(null != cache)
		{
			if(segmentList.size() < segment.total)
			{
				Set<Integer> totalKeys = new HashSet<Integer>();
				for(int i =0; i<segment.total;i++)
				{
					totalKeys.add(i);
				}
				for(EventSegment seg:segmentList)
				{
					totalKeys.remove(seg.sequence);
				}
				for(Integer key:totalKeys)
				{
					byte[] content = (byte[]) cache.get(key);
					Buffer buf = Buffer.wrapReadableContent(content);
					EventSegment seg;
                    try
                    {
	                    seg = (EventSegment) EventDispatcher.getSingletonInstance().parse(buf);
	                    segmentList.add(seg);
                    }
                    catch (Exception e)
                    {
                    	sessionBufferTable.clear();
                    	return null;
                    }					
				}
			}
		}
		if (segmentList.size() == segment.total)
		{
			Collections.sort(segmentList);
			Buffer content = new Buffer(4096);
			for (EventSegment seg : segmentList)
			{
				content.write(seg.content, seg.content.readableBytes());
			}
			sessionBufferTable.remove(segment.getHash());
			return content;
		}
		return null;
	}
}
