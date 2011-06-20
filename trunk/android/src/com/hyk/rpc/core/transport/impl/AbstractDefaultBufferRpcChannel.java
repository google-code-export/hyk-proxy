/**
 * This file is part of the hyk-rpc project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AbstractDefaultBufferRpcChannel.java 
 *
 * @author qiying.wang [ Apr 23, 2010 | 3:01:13 PM ]
 *
 */
package com.hyk.rpc.core.transport.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.serializer.impl.SerailizerStream;

/**
 *
 */
public abstract class AbstractDefaultBufferRpcChannel extends
		AbstractDefaultRpcChannel
{
	protected static final int HEADER_LEN = 6;
	protected static final short						START_TAG	= 0xCA;

	protected Address								localAddr;

	protected Map<Address, BufferRpcChannelData>	readBuffer	= new ConcurrentHashMap<Address, BufferRpcChannelData>();
	protected List<RpcChannelData>  msgList = new LinkedList<RpcChannelData>();
	protected BufferRpcChannelData					writeBuffer;

	protected enum State
	{
		READ_HEADER, READ_CONTENT
	}

	class BufferRpcChannelData
	{
	
		State state = State.READ_HEADER;
		byte[] header = new byte[HEADER_LEN];
		int headCursor = 0;
		byte[] content = null;
		int     contentCursor = 0;
		int		contentLength;
		BufferRpcChannelData()
		{
		}
		
        private void clearState()
        {
        	contentLength = 0;
			contentCursor = 0;
			headCursor = 0;
			content = null;
			state = State.READ_HEADER;
        }
		
		public ChannelDataBuffer[] addBuffer(ChannelDataBuffer buf) throws IOException
		{
			if(!buf.readable())
			{
				return null;
			}
			if(logger.isDebugEnabled())
			{
				logger.debug("Try to construct buffer:" + buf.readableBytes() + ", at state:" + state);
			}
			switch (state)
			{
				case READ_HEADER:
				{
					int readHeaderLen = 0;
					if((buf.readableBytes() + headCursor) < HEADER_LEN)
					{
						readHeaderLen = buf.readableBytes();
					}
					else
					{
						readHeaderLen = (HEADER_LEN - headCursor);
					}
					int len = buf.getInputStream().read(header, headCursor, readHeaderLen);
					headCursor += len;
					if(headCursor < HEADER_LEN)
					{
						return null;
					}
					else
					{
						ByteBuffer hb = ByteBuffer.wrap(header);
						short tag = hb.getShort();
						if(tag != START_TAG)
						{
							clearState();
							throw new IOException("Unexpected message with header tag:" + tag);
						}
						contentLength = hb.getInt();
						if(logger.isDebugEnabled())
						{
							logger.debug("ContentLength is " + contentLength);
						}
						state = State.READ_CONTENT;
					}
				}
				case READ_CONTENT:
				{
					if(null == content)
					{
						content = new byte[contentLength];
					}
					if(buf.readable())
					{
						int readContentLen = 0;
						if((buf.readableBytes() + contentCursor) < contentLength)
						{
							readContentLen = buf.readableBytes();
						}
						else
						{
							readContentLen = (contentLength - contentCursor);
						}
						int len = buf.getInputStream().read(content, contentCursor, readContentLen);
						contentCursor += len;
						if(contentCursor == contentLength)
						{
							ChannelDataBuffer ret = ChannelDataBuffer.wrap(content);
							clearState();
							return new ChannelDataBuffer[]{ret, buf};
						}
					}
				}
			}
			return null;
		}

	}

	public AbstractDefaultBufferRpcChannel(Executor threadPool)
	{
		super(threadPool);
	}

	@Override
	public Address getRpcChannelAddress()
	{
		return localAddr;
	}

	private void addRawDataToReadBuffer(RpcChannelData raw) throws IOException
	{
		BufferRpcChannelData buf = readBuffer.get(raw.address);
		if (null == buf)
		{
			buf = new BufferRpcChannelData();
			readBuffer.put(raw.address, buf);
		} 
		ChannelDataBuffer[] ret = buf.addBuffer(raw.content);
		do
		{
			RpcChannelData data = new RpcChannelData(ret[0], raw.address);
			msgList.add(data);
			ret = buf.addBuffer(ret[1]);
		}while(ret != null);
	}

	protected abstract RpcChannelData recvRawData() throws IOException;

	protected abstract void sendRawData(RpcChannelData data) throws IOException;

	@Override
	protected final RpcChannelData recv() throws IOException
	{
		while(msgList.isEmpty())
		{
			addRawDataToReadBuffer(recvRawData());
		}
		return msgList.remove(0);
	}

	@Override
	protected final void send(RpcChannelData data) throws IOException
	{
		ByteBuffer headbuf = ByteBuffer.allocate(HEADER_LEN);
		headbuf.putShort(START_TAG);

		headbuf.putInt(data.content.readableBytes());
		ChannelDataBuffer header = ChannelDataBuffer.wrap(headbuf.array());
		ChannelDataBuffer all = ChannelDataBuffer.wrap(header, data.content);
		data = new RpcChannelData(all, data.address);
        sendRawData(data);
	}

}
