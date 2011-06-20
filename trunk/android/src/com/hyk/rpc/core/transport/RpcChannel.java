/**
 * 
 */
package com.hyk.rpc.core.transport;

import java.io.IOException;
import java.io.NotSerializableException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.compress.CompressorFactory;
import com.hyk.compress.compressor.Compressor;
import com.hyk.compress.compressor.none.NoneCompressor;
import com.hyk.compress.preference.CompressPreference;
import com.hyk.compress.preference.EmptyCompressPreference;
import com.hyk.io.buffer.ChannelDataBuffer;

import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.constant.RpcConstants;
import com.hyk.rpc.core.message.Message;
import com.hyk.rpc.core.message.MessageFragment;
import com.hyk.rpc.core.message.MessageID;
import com.hyk.rpc.core.session.SessionManager;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;
import com.hyk.serializer.impl.SerailizerStream;
import com.hyk.serializer.util.ContextUtil;
import com.hyk.util.thread.ThreadLocalUtil;

/**
 * @author qiying.wang
 * 
 */
public abstract class RpcChannel
{
	protected Logger				logger				= LoggerFactory.getLogger(getClass());

	protected static final byte[]	MAGIC_HEADER		= "@hyk-rpc@".getBytes();
	protected static final int		GAP					= 32;

	protected byte[]				magicHeader			= new byte[MAGIC_HEADER.length];

	protected int					maxMessageSize		= 2048;

	protected List<MessageFragment>	sendList			= new LinkedList<MessageFragment>();
	protected Serializer			serializer			= new HykSerializer();
	protected Executor				threadPool;
	protected SessionManager		sessionManager;
	
	protected boolean running = true;

	// protected List<MessageListener> msgListeners = new
	// LinkedList<MessageListener>();
	protected MessageListener		msgListener;

	protected OutputTask			outTask				= new OutputTask();
	protected InputTask				inTask				= new InputTask();
	protected boolean				isStarted			= false;

	protected CompressPreference	compressPreference	= new EmptyCompressPreference();

	// protected CompressorFactory compressorFactory = new CompressorFactory();

	public RpcChannel()
	{
		this(null);
	}

	public RpcChannel(Executor threadPool)
	{
		this.threadPool = threadPool;
	}

	public void configure(Properties initProps) throws Exception
	{
		String compressPreferClassName = null != initProps ? initProps.getProperty(RpcConstants.COMPRESS_PREFER) : null;
		if(null != compressPreferClassName)
		{
			compressPreference = (CompressPreference)Class.forName(compressPreferClassName).newInstance();
		}
	}

	public synchronized void start()
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("RpcChannel start.");
		}
		if(isStarted)
		{
			return;
		}
		isStarted = true;
		if(null != threadPool)
		{
			threadPool.execute(inTask);
			threadPool.execute(outTask);
		}

	}

	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
		registerMessageListener(sessionManager);
	}

	public final void registerMessageListener(MessageListener listener)
	{
		//msgListeners.add(listener);
		this.msgListener = listener;
	}

	public void setMaxMessageSize(int maxMessageSize)
	{
		this.maxMessageSize = maxMessageSize;
	}

	public abstract Address getRpcChannelAddress();

	public abstract boolean isReliable();

	protected abstract void saveMessageFragment(MessageFragment fragment);

	protected abstract MessageFragment[] loadMessageFragments(MessageID id);

	protected abstract void deleteMessageFragments(MessageID id);

	protected abstract RpcChannelData recv() throws IOException;

	protected abstract void send(RpcChannelData data) throws IOException;

	public void close()
	{
		running = false;
	}

	public final void clearSessionData(MessageID sessionID)
	{
		deleteMessageFragments(sessionID);
	}

	public final void sendMessage(Message message) throws NotSerializableException, IOException
	{
		ChannelDataBuffer data = serializer.serialize(message);
		int size = data.readableBytes();
		if(logger.isDebugEnabled())
		{
			logger.debug("send message " + message.getValue() + " to " + message.getAddress().toPrintableString() + " with total size:" + size);
		}
		int msgFragsount = size / maxMessageSize;
		if(size % maxMessageSize > 0)
		{
			msgFragsount++;
		}

		int off = 0;
		int len = 0;
		for(int i = 0; i < msgFragsount; i++)
		{
			ChannelDataBuffer sent = null;
			if(msgFragsount == 1)
			{
				sent = data;
			}
			else
			{
				if(size - off >= maxMessageSize)
				{
					len = maxMessageSize;
				}
				else
				{
					len = size - off;
				}
				off += len;

				// sent = ByteBuffer.allocate(len);
				byte[] raw = new byte[len];
				data.readBytes(raw);
				sent = ChannelDataBuffer.wrap(raw);
				// sent = raw;
				//sent = ByteBuffer.wrap(raw);
			}

			MessageFragment fragment = new MessageFragment();
			fragment.setAddress(message.getAddress());
			fragment.setSessionID(message.getSessionID());
			fragment.setSequence(i);
			fragment.setTotalFragmentCount(msgFragsount);
			fragment.setContent(sent);
			if(logger.isDebugEnabled())
			{
				logger.debug("Send message with size:" + sent.capacity() + ", fragments count:" + msgFragsount);
			}
			// when the send list empty ,send data directly
			if(null != threadPool && !sendList.isEmpty())
			{
				synchronized(sendList)
				{
					sendList.add(fragment);
					sendList.notify();
				}
			}
			else
			{
				sendMessageFragment(fragment);
			}
		}
		// if(msgFragsount > 1)
		// {
		// //data.free();
		// }
	}

	public final void processIncomingData(RpcChannelData data) throws RpcChannelException
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Process message from " + data.address.toPrintableString());
		}
		Message msg = processRpcChannelData(data);
		if(msg != null && null != msgListener)
		{
//			for(final MessageListener messageListener : msgListeners)
//			{
//				messageListener.onMessage(msg);
//			}
			msgListener.onMessage(msg);
		}
	}

	private void dispatch(final Message msg)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Dispatch message!");
		}

		//for(final MessageListener messageListener : msgListeners)
		{
			threadPool.execute(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if(null != msgListener)
						{
							msgListener.onMessage(msg);
						}
					}
					catch(Exception e)
					{
						logger.error("Failed to process message.", e);
					}

				}
			});
		}
	}

	protected void sendMessageFragment(MessageFragment msg) throws IOException
	{
		// ByteArray data = ByteArray.allocate(maxMessageSize + GAP);
		// data.put(MAGIC_HEADER);
		int baseSize = msg.getContent().capacity();
		ChannelDataBuffer data = ChannelDataBuffer.allocate(baseSize + 2 * GAP);
		// data.put(MAGIC_HEADER);
		data.getOutputStream().write(MAGIC_HEADER);
		int compressorId = CompressorFactory.getRegistCompressor(compressPreference.getCompressor().getName()).id;
		SerailizerStream.writeInt(data, compressorId);
		if(compressorId == CompressorFactory.getRegistCompressor(NoneCompressor.NAME).id)
		{
			serializer.serialize(msg, data);
		}
		else
		{
			ChannelDataBuffer seriaData = ChannelDataBuffer.allocate(baseSize + GAP);
			seriaData = serializer.serialize(msg, seriaData);
			if(logger.isDebugEnabled())
			{
				logger.debug("Send/Before compressing, data size:" + seriaData.readableBytes());
			}
			ChannelDataBuffer newData = compressPreference.getCompressor().compress(seriaData, data);
			if(logger.isDebugEnabled())
			{
				logger.debug("Send/After compressing, data size:" + newData.readableBytes());
			}
		}
		// ByteDataBuffer seriaData = ByteDataBuffer.allocate(baseSize + GAP);
		// seriaData = serializer.serialize(msg, seriaData);
		// msg.getContent().free();
		// if(seriaData.size() > compressPreference.getTrigger())
		// {
		// if(logger.isDebugEnabled())
		// {
		// logger.debug("Send/Before compressing, data size:" +
		// seriaData.size());
		// }
		// int compressorId =
		// CompressorFactory.getRegistCompressor(compressPreference.getCompressor().getName()).id;
		// SerailizerStream.writeInt(data, compressorId);
		// ByteDataBuffer newData =
		// compressPreference.getCompressor().compress(seriaData);
		// if(logger.isDebugEnabled())
		// {
		// logger.debug("Send/After compressing, data size:" + newData.size());
		// }
		// // if(newData != seriaData)
		// // {
		// // seriaData.free();
		// // }
		// data.put(newData);
		// //newData.free();
		// }
		// else
		// {
		// SerailizerStream.writeInt(data,
		// CompressorFactory.getRegistCompressor(NoneCompressor.NAME).id);
		// data.put(seriaData);
		// //data.getOutputStream().write(seriaData);
		// //seriaData.free();
		// }

		data.flip();
		RpcChannelData send = new RpcChannelData(data, msg.getAddress());
		send(send);
		// data.free();
	}

	protected synchronized Message processRpcChannelData(RpcChannelData data) throws RpcChannelException
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Recv data from " + data.address + ", data size is " + data.content.readableBytes());
		}
		ChannelDataBuffer oldContent = data.content;
		oldContent.readBytes(magicHeader);
		// oldContent.getInputStream().read(magicHeader);
		if(!Arrays.equals(magicHeader, MAGIC_HEADER))
		{
			// String x = new String(oldContent.buffer().array(), 0,
			// oldContent.buffer().limit());
			// oldContent.free();
			throw new RpcChannelException("Unexpected rpc message with bad header!" + new String(magicHeader));
		}
		try
		{
			int compressorTypeValue = SerailizerStream.readInt(oldContent);
			Compressor compressor = CompressorFactory.getRegistCompressor(compressorTypeValue).compressor;
			// Compressor compressor = new NoneCompressor();
			if(logger.isDebugEnabled())
			{
				logger.debug("Recv/Before decompressing, data size:" + oldContent.readableBytes());
			}
			ChannelDataBuffer content = compressor.decompress(oldContent);
			if(logger.isDebugEnabled())
			{
				logger.debug("Recv/After decompressing, data size:" + content.readableBytes());
			}
			// if(content != oldContent)
			// {
			// oldContent.free();
			// }
			
			ThreadLocalUtil.getThreadLocalUtil(SessionManager.class).setThreadLocalObject(sessionManager);
			ContextUtil.setDeserializeClassLoader(getClass().getClassLoader());
			MessageFragment fragment = serializer.deserialize(MessageFragment.class, content);
			fragment.setAddress(data.address);
			if(logger.isDebugEnabled())
			{
				logger.debug("Recv " + fragment + " with size:" + content.readableBytes());
			}
			// content.free();
			if(fragment.getTotalFragmentCount() == 1 && fragment.getSequence() == 0)
			{
				Message msg = serializer.deserialize(Message.class, fragment.getContent());
				// fragment.getContent().free();

				msg.setAddress(data.address);
				msg.setSessionID(fragment.getSessionID());
				return msg;
			}

			saveMessageFragment(fragment);
			MessageFragment[] fragments = loadMessageFragments(fragment.getId());
			if(fragments.length != fragment.getTotalFragmentCount())
			{
				deleteMessageFragments(fragment.getId());
				throw new RpcChannelException("Fragments length is " + fragments.length + ", and total count is " + fragment.getTotalFragmentCount());
			}
			int totalSize = 0;
			for(int i = 0; i < fragments.length; i++)
			{
				if(null == fragments[i])
				{
					if(logger.isDebugEnabled())
					{
						logger.debug("Message is not ready to process since element:" + i + " is null.");
					}
					return null;
				}
				totalSize += fragments[i].getContent().capacity();
			}
			if(logger.isDebugEnabled())
			{
				logger.debug("Message is ready to process!");
			}
			ChannelDataBuffer[] msgBuffers = new ChannelDataBuffer[fragments.length];
			for(int i = 0; i < fragments.length; i++)
			{
				// msgBuffer.getOutputStream().write(fragments[i].getContent());
				msgBuffers[i] = fragments[i].getContent();
			}
			//msgBuffer.flip();
			deleteMessageFragments(fragment.getId());

			Message msg = serializer.deserialize(Message.class, ChannelDataBuffer.wrap(msgBuffers));
			msg.setSessionID(fragment.getSessionID());
			msg.setAddress(data.address);
			// msgBuffer.free();

			return msg;
		}
		catch(RpcChannelException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new RpcChannelException("Failed to process message!", e);
		}

	}

	class InputTask implements Runnable
	{
		@Override
		public void run()
		{
			while(running)
			{
				try
				{
					RpcChannelData data = recv();
					if(null == data)
					{
						continue;
					}
					Message msg = processRpcChannelData(data);
					if(null != msg)
					{
						dispatch(msg);
					}
				}
				catch(Throwable e)
				{
					logger.error("Failed to process received message", e);
				}
			}
		}
	}

	class OutputTask implements Runnable
	{
		@Override
		public void run()
		{
			while(running)
			{
				try
				{
					MessageFragment msg = null;
					synchronized(sendList)
					{
						if(sendList.isEmpty())
						{
							sendList.wait(1000);
						}
						if(!sendList.isEmpty())
						{
							msg = sendList.remove(0);
						}
					}
					if(null != msg)
					{
						sendMessageFragment(msg);
					}
				}
				catch(Throwable e)
				{
					logger.error("Failed to send message", e);
				}
			}
		}
	}
}
