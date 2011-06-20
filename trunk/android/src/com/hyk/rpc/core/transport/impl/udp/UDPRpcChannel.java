/**
 * 
 */
package com.hyk.rpc.core.transport.impl.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executor;

import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.rpc.core.address.Address;
import com.hyk.rpc.core.address.SimpleSockAddress;
import com.hyk.rpc.core.transport.RpcChannelData;
import com.hyk.rpc.core.transport.impl.AbstractDefaultRpcChannel;

/**
 * @author Administrator
 * 
 */
public class UDPRpcChannel extends AbstractDefaultRpcChannel
{
	private ByteBuffer			recvBuffer	= ByteBuffer.allocateDirect(65536);
	private DatagramChannel		channel;
	private SimpleSockAddress	localAddr;

	public UDPRpcChannel(Executor threadPool, int port) throws IOException
	{
		super(threadPool);
		channel = DatagramChannel.open();
		channel.socket().bind(new InetSocketAddress(port));
		localAddr = new SimpleSockAddress(InetAddress.getLocalHost().getHostAddress(), port);
		start();
	}

	@Override
	public Address getRpcChannelAddress()
	{
		return localAddr;
	}

	@Override
	protected RpcChannelData recv() throws IOException
	{
		recvBuffer.clear();
		InetSocketAddress target = (InetSocketAddress)channel.receive(recvBuffer);
		recvBuffer.flip();
		
		SimpleSockAddress address = new SimpleSockAddress(target.getAddress().getHostAddress(), target.getPort());
		ChannelDataBuffer data = ChannelDataBuffer.allocate(recvBuffer.limit());
		data.writeBytes(recvBuffer);
		data.flip();
		return new RpcChannelData(data, address);

	}

	@Override
	protected void send(RpcChannelData data) throws IOException
	{
		SimpleSockAddress address = (SimpleSockAddress)data.address;
		InetSocketAddress addr = new InetSocketAddress(address.getHost(), address.getPort());
		channel.send(ChannelDataBuffer.asByteBuffer(data.content), addr);
	}

	@Override
	public boolean isReliable()
	{
		return false;
	}

}
