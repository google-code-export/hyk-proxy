/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RangeChunkedInput.java 
 *
 * @author qiying.wang [ Feb 2, 2010 | 10:53:27 AM ]
 *
 */
package com.hyk.proxy.gae.client.netty;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.stream.ChunkedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.gae.client.config.Config;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.proxy.gae.common.http.RangeHeaderValue;
import com.hyk.rpc.core.Rpctimeout;

/**
 *
 */
public class RangeHttpProxyChunkedInput implements ChunkedInput
{
	protected Logger				logger			= LoggerFactory.getLogger(getClass());
	private final FetchServiceSelector fetchServiceSelector;
	private HttpRequestExchange	forwardRequest;
	private final int retryTimes = 3;
	
	
	private LinkedList<FetchResponse> fetchedResponses;
	private long					step;
	private long                    fetchnum;
	private volatile int            nextFetcherSequence;
	private long                    fetchCounter;
	private int                     chunkSequence;
	private ExecutorService workers = Executors.newFixedThreadPool(5);
	private boolean isClose  = false;
	private final long offset;
	private final long total;

	
	public RangeHttpProxyChunkedInput(FetchServiceSelector fetchServiceSelector, HttpRequestExchange forwardRequest, long offset, long total) throws IOException
	{
		this.total = total;
		this.fetchServiceSelector = fetchServiceSelector;
		this.forwardRequest = forwardRequest;
		this.offset = offset;
		step = Config.getInstance().getFetchLimitSize();
		this.fetchnum = (total - offset) /step;
		if((total - offset)%step != 0)
		{
			fetchnum++;
		}
		if(logger.isDebugEnabled())
		{
			logger.debug("Total fetch number is " + fetchnum);
		}
		fetchedResponses  = new LinkedList<FetchResponse>();
		chunkSequence = 0;
		workers = Executors.newFixedThreadPool(Config.getInstance().getMaxFetcherForBigFile());
		for (int i = 0; i < Config.getInstance().getMaxFetcherForBigFile() && i < fetchnum ; i++) 
		{
			workers.execute(new RangeFetch(i));
			nextFetcherSequence++;
		}
		//worke
	}
	
	private HttpRequestExchange getCloneForwardHttpRequestExchange()
	{
		HttpRequestExchange ret = new HttpRequestExchange();
		ret.setMethod(forwardRequest.getMethod());
		ret.setHeaders(forwardRequest.getCloneHeaders());
		ret.setBody(forwardRequest.getBody());
		ret.setUrl(forwardRequest.getUrl());
		return ret;
	}
	
	@Override
	public void close() throws Exception
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Close this RangeHttpProxyChunkedInput");
		}
		isClose = true;
		workers.shutdown();
	}

	@Override
	public synchronized boolean hasNextChunk() throws Exception
	{
		return chunkSequence < fetchnum;
	}

	
	@Override
	public Object nextChunk() throws Exception
	{
		HttpResponseExchange response = null;
		//List<byte[]> rawBodys = new LinkedList<byte[]>();
		synchronized (fetchedResponses) 
		{
			while(fetchedResponses.isEmpty())
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Wait for next chunk since recv list is null.");
				}
				fetchedResponses.wait();
			}
			while(chunkSequence != fetchedResponses.getFirst().sequence)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Wait since latest sequence = "+fetchedResponses.getFirst().sequence + " and chunkSequence = " + chunkSequence);
				}
				fetchedResponses.wait();
			}
			if(logger.isDebugEnabled())
			{
				logger.debug("Write chunk with sequnce " + chunkSequence);
			}
			chunkSequence++;
			response = fetchedResponses.removeFirst().response;
			
			
		}
		return ChannelBuffers.wrappedBuffer(response.getBody());
		
		
	}
	
	private boolean hasFetchedAll()
	{
		return fetchCounter < fetchnum;
	}
	
	class FetchResponse
	{
		int sequence;
		HttpResponseExchange response;
		public FetchResponse(int sequence, HttpResponseExchange response)
		{
			this.sequence = sequence;
			this.response = response;
		}
	}
	
	class RangeFetch implements Runnable
	{
		private int sequence;
		
		RangeFetch(int sequence)
		{
			this.sequence = sequence;
		}
		@Override
		public void run() 
		{
			try 
			{
				while(hasFetchedAll() && nextFetcherSequence <= fetchnum && !isClose)
				{
					long start = (sequence)* step + offset;
					if(start >= total)
					{
						return;
					}
					long nextPos = start + step - 1;
					if(nextPos >= total)
					{
						nextPos =  total-1;
					}
					RangeHeaderValue headerValue = new RangeHeaderValue(start, nextPos);
					HttpRequestExchange rangeReq =  getCloneForwardHttpRequestExchange();
					rangeReq.setHeader(HttpHeaders.Names.RANGE, headerValue);
					if(logger.isDebugEnabled())
					{
						logger.debug("Send range proxy request");
						logger.debug(rangeReq.toPrintableString());
					}
					HttpResponseExchange res = null;
					int retry = retryTimes;
					while(null == res && retry > 0)
					{
						try 
						{
							res = fetchServiceSelector.select().fetch(rangeReq);
						} 
						catch (Rpctimeout e) 
						{
							if(logger.isDebugEnabled())
							{
								logger.debug("Fetch encounter timeout, retry one time.");
							}
							retry--;
						}
					}
					
					if(logger.isDebugEnabled())
					{
						logger.debug("Recv range proxy response");
						logger.debug(res.toPrintableString());
					}
					synchronized (fetchedResponses) 
					{
						boolean hasAdd = false;
						for (int i = 0; i < fetchedResponses.size(); i++) 
						{
							FetchResponse other = fetchedResponses.get(i);
							if(other.sequence > sequence)
							{
								fetchedResponses.add(i, new FetchResponse(sequence,res));
								fetchedResponses.notify();
								hasAdd = true;
								break;
							}
							
						}
						if(!hasAdd)
						{
							fetchedResponses.add(new FetchResponse(sequence,res));
							fetchedResponses.notify();
						}
						if(logger.isDebugEnabled())
						{
							logger.debug("Fetch success with sequence = "+sequence + "@" +  Thread.currentThread());
						}
						fetchCounter++;
						sequence = nextFetcherSequence;
						nextFetcherSequence++;
					}
				}
			} 
			catch (Exception e1) 
			{
				fetchCounter = fetchnum;
				logger.error("Failed for this fetch thread!", e1);
				
			}	
		}
		
	}
}
