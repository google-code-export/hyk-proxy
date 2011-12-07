/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: EncryptEvent.java 
 *
 * @author yinqiwen [ 2011-12-3 | ÏÂÎç02:13:29 ]
 *
 */
package org.hyk.proxy.gae.common.event;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.encrypt.SimpleEncrypt;
import org.arch.event.Event;
import org.arch.event.EventDispatcher;
import org.arch.event.EventType;
import org.arch.event.EventVersion;
import org.hyk.proxy.gae.common.EncryptType;
import org.hyk.proxy.gae.common.GAEConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@EventType(GAEConstants.ENCRYPT_EVENT_TYPE)
@EventVersion(1)
public class EncryptEvent extends Event
{
	protected static Logger logger = LoggerFactory
	        .getLogger(EncryptEvent.class);

	public EncryptEvent()
	{
	}

	public EncryptEvent(EncryptType type, Event ev)
	{
		this.type = type;
		this.ev = ev;
	}

	public EncryptType type;
	public Event ev;

	@Override
	protected boolean onDecode(Buffer buffer)
	{
		int t;
		try
		{
			t = BufferHelper.readVarInt(buffer);
			type = EncryptType.fromInt(t);
			Buffer content = buffer;
			switch (type)
			{
				case SE1:
				{
					SimpleEncrypt se1 = new SimpleEncrypt();
					content = se1.decrypt(buffer);
					break;
				}
				default:
				{
					break;
				}
			}
			ev = EventDispatcher.getSingletonInstance().parse(content);
			return true;
		}
		catch (Exception e)
		{
			logger.error("Failed to decode encrypt event", e);
			return false;
		}
	}

	@Override
	protected boolean onEncode(Buffer buffer)
	{
		BufferHelper.writeVarInt(buffer, type.getValue());
		Buffer content = new Buffer(256);
		ev.encode(content);
		switch (type)
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
		buffer.write(content, content.readableBytes());
		return true;
	}
}
