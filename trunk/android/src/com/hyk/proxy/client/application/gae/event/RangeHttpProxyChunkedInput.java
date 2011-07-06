/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RangeChunkedInput.java 
 *
 * @author yinqiwen [ Feb 2, 2010 | 10:53:27 AM ]
 *
 */
package com.hyk.proxy.client.application.gae.event;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.hyk.proxy.android.config.Config;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.stream.ChunkedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.client.application.gae.event.GoogleAppEngineHttpProxyEventServiceFactory.FetchServiceSelector;
import com.hyk.proxy.client.util.ClientUtils;
import com.hyk.proxy.common.http.header.RangeHeaderValue;
import com.hyk.proxy.common.http.message.HttpRequestExchange;
import com.hyk.proxy.common.http.message.HttpResponseExchange;
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
	private int                    fetchnum;
	private AtomicInteger            nextFetcherSequence  = new AtomicInteger(0);
	private AtomicInteger                    fetchCounter   = new AtomicInteger(0);
	private AtomicInteger                     chunkSequence = new AtomicInteger(0);
	private Executor workers ;
	private volatile boolean isClose  = false;
	private final long offset;
	private final long total;

	
	public RangeHttpProxyChunkedInput(FetchServiceSelector fetchServiceSelector, Executor workerExecutor, HttpRequestExchange forwardRequest, long offset, long total) throws IOException
	{
		this.workers = workerExecutor;
		this.total = total;
		this.fetchServiceSelector = fetchServiceSelector;
		this.forwardRequest = forwardRequest;
		this.offset = offset;
		step = Config.getInstance().getFetchLimitSize();
		this.fetchnum = (int)((total - offset) /step);
		if((total - offset)%step != 0)
		{
			fetchnum++;
		}
		if(logger.isDebugEnabled())
		{
			logger.debug("Total fetch number is " + fetchnum);
		}
		fetchedResponses  = new LinkedList<FetchResponse>();
		workers = Executors.newFixedThreadPool(Config.getInstance().getMaxFetcherNumber());
		for (int i = 0; i < Config.getInstance().getMaxFetcherNumber() && i < fetchnum ; i++) 
		{
			workers.execute(new RangeFetch(i));
			nextFetcherSequence.set(i + 1);
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
	public synchronized void close() throws Exception
	{
		if(!isClose)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Close this RangeHttpProxyChunkedInput");
			}
			closeChunkInput();
			//workers.shutdown();
		}
		
	}

	
	@Override
	public synchronized boolean hasNextChunk() throws Exception
	{
		return chunkSequence.get() < fetchnum;
	}

	
	@Override
	public Object nextChunk() throws Exception
	{
		HttpResponseExchange response = null;
		synchronized (fetchedResponses) 
		{
			while(!isClose && (fetchedResponses.isEmpty() || chunkSequence.get() != fetchedResponses.getFirst().sequence))
			{
				if(logger.isDebugEnabled())
				{
					if(fetchedResponses.isEmpty())
					{
						logger.debug("Wait for next chunk since recv list is null.");
					}
					else
					{
						logger.debug("Wait recv list is null or latest sequence = "+fetchedResponses.getFirst().sequence + " and chunkSequence = " + chunkSequence);
					}			
				}
				fetchedResponses.wait();
			}
			if(isClose || fetchedResponses.isEmpty() || chunkSequence.get() != fetchedResponses.getFirst().sequence)
			{
				return ChannelBuffers.EMPTY_BUFFER;
			}
				
			chunkSequence.incrementAndGet();
			response = fetchedResponses.removeFirst().response;
			if(logger.isDebugEnabled())
			{
				logger.debug("Write chunk with sequnce " + chunkSequence.get() + " and content-range:" + response.getHeaderValue("content-range") + response.getHeaderValue("content-type"));
			}
		}
		ChannelBuffer ret = ChannelBuffers.wrappedBuffer(response.getBody());
		return ret;
	}
	
	private boolean hasFetchedAll()
	{
		return fetchCounter.get() >= fetchnum;
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
	
	protected void closeChunkInput()
	{
		isClose = true;
		chunkSequence.set(fetchnum);
		synchronized (fetchedResponses) 
		{
			fetchedResponses.notify();
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
				while(!hasFetchedAll() && !isClose)
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
						logger.debug(ClientUtils.httpMessage2String(rangeReq));
					}
					HttpResponseExchange res = null;
					int retry = retryTimes;
					while((null == res || res.getHeaderValue(HttpHeaders.Names.CONTENT_RANGE) == null) && retry > 0)
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
						}
						retry--;
					}
					if(null != res && logger.isDebugEnabled())
					{
						logger.debug("Recv range proxy response");
						logger.debug(ClientUtils.httpMessage2String(res));
					}
					if(null == res || res.getHeaderValue(HttpHeaders.Names.CONTENT_RANGE) == null)
					{
						logger.error("Faile to fetch a chunk for range:"+headerValue);
						closeChunkInput();
						return;
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
								hasAdd = true;
								break;
							}
						}
						if(!hasAdd)
						{
							fetchedResponses.add(new FetchResponse(sequence,res));
						}
						fetchedResponses.notify();
						if(logger.isDebugEnabled())
						{
							logger.debug("Fetch success with sequence = "+sequence + "@" +  Thread.currentThread());
						}
						fetchCounter.incrementAndGet();
						sequence = nextFetcherSequence.getAndIncrement();
					}
				}
			} 
			catch (Exception e1) 
			{
				logger.error("Failed for this fetch thread!", e1);
				closeChunkInput();
			}	
		}
		
	}
}
