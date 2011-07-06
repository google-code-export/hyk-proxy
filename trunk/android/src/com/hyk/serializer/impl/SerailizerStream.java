/**
 * 
 */
package com.hyk.serializer.impl;

import java.io.IOException;
import java.io.NotSerializableException;

import com.hyk.serializer.SerializerInput;
import com.hyk.serializer.SerializerOutput;
import com.hyk.serializer.io.Type;
import com.hyk.serializer.reflect.ReflectionCache;
import com.hyk.serializer.util.ContextUtil;
import com.hyk.io.buffer.ChannelDataBuffer;
import com.hyk.util.reflect.ClassUtil;

/**
 * @author qiying.wang
 * @param <T>
 * 
 */
public abstract class SerailizerStream<T>
{

	private static final int	INDICATOR_DEFAULT	= 0;
	private static final int	INDICATOR_TYPE		= 1;
	private static final int	INDICATOR_PROXY		= 2;
	private static final int	INDICATOR_OTHER		= 3;
	private static final int	INDICATOR_LOOP_REF	= 4;
	private static final int	INDICATOR_NULL	= 5;
	
	protected static class Input implements SerializerInput
	{
		private ChannelDataBuffer	data;

		public Input(ChannelDataBuffer data)
		{
			this.data = data;
		}

		@Override
		public boolean readBoolean() throws IOException
		{
			return readBool(data);
		}

		@Override
		public byte readByte() throws IOException
		{
			return SerailizerStream.readByte(data);
		}

		@Override
		public char readChar() throws IOException
		{
			return SerailizerStream.readChar(data);
		}

		@Override
		public double readDouble() throws IOException
		{
			return SerailizerStream.readDouble(data);
		}

		@Override
		public float readFloat() throws IOException
		{
			return SerailizerStream.readFloat(data);
		}

		@Override
		public int readInt() throws IOException
		{
			return SerailizerStream.readInt(data);
		}

		@Override
		public long readLong() throws IOException
		{
			return SerailizerStream.readLong(data);
		}

		@Override
		public short readShort() throws IOException
		{
			return SerailizerStream.readShort(data);
		}

		@Override
		public String readString() throws IOException
		{
			return SerailizerStream.readString(data);
		}

		public byte[] readBytes() throws IOException
		{
			return SerailizerStream.readBytes(data);
		}
		public void readBytes(byte[] content, int off, int len) throws IOException
		{
			SerailizerStream.readBytes(data, content, off, len);
		}
		
		public void readBytes(byte[] content) throws IOException
		{
			SerailizerStream.readBytes(data, content);
		}

		@Override
		public <T> T readObject(Class<T> type) throws IOException
		{
			return SerailizerStream.readObject(data, type);
		}
	}

	protected static class Output implements SerializerOutput
	{
		private ChannelDataBuffer	data;

		public Output(ChannelDataBuffer data)
		{
			this.data = data;
		}

		@Override
		public void writeBoolean(boolean value) throws IOException
		{
			SerailizerStream.writeBoolean(data, value);
		}

		@Override
		public void writeByte(byte value) throws IOException
		{
			SerailizerStream.writeByte(data, value);
		}

		@Override
		public void writeChar(char value) throws IOException
		{
			SerailizerStream.writeChar(data, value);
		}

		@Override
		public void writeDouble(double value) throws IOException
		{
			SerailizerStream.writeDouble(data, value);
		}

		@Override
		public void writeFloat(float value) throws IOException
		{
			SerailizerStream.writeFloat(data, value);
		}

		@Override
		public void writeInt(int value) throws IOException
		{
			SerailizerStream.writeInt(data, value);
		}

		@Override
		public void writeLong(long value) throws IOException
		{
			SerailizerStream.writeLong(data, value);
		}

		@Override
		public void writeShort(short value) throws IOException
		{
			SerailizerStream.writeShort(data, value);
		}

		@Override
		public void writeString(String value) throws IOException
		{
			SerailizerStream.writeString(data, value);
		}

		@Override
		public void writeObject(Object value) throws IOException
		{
			SerailizerStream.writeObject(data, value);
		}

		@Override
		public void writeObject(Object value, Class declType) throws IOException
		{
			SerailizerStream.writeObject(data, value, declType);
		}

		public void writeBytes(byte[] value) throws IOException
		{
			if(null != value)
			{
				writeBytes(value, 0, value.length);
			}
		}

		public void writeBytes(byte[] value, int off, int len) throws IOException
		{
			if(null != value)
			{
				SerailizerStream.writeInt(data, len);
				writeRawBytes(value, off, len);
			}
		}

		@Override
		public void writeRawBytes(byte[] value, int off, int len) throws IOException
		{
			if(null != value)
			{
				SerailizerStream.writeBytes(data, value, off, len);
			}
		}
	}

	protected static int readRawLittleEndian32(ChannelDataBuffer data) throws IOException
	{
		final byte b1 = readByte(data);
		final byte b2 = readByte(data);
		final byte b3 = readByte(data);
		final byte b4 = readByte(data);
		return (((int)b1 & 0xff)) | (((int)b2 & 0xff) << 8) | (((int)b3 & 0xff) << 16) | (((int)b4 & 0xff) << 24);
	}

	protected static long readRawLittleEndian64(ChannelDataBuffer data) throws IOException
	{
		final byte b1 = readByte(data);
		final byte b2 = readByte(data);
		final byte b3 = readByte(data);
		final byte b4 = readByte(data);
		final byte b5 = readByte(data);
		final byte b6 = readByte(data);
		final byte b7 = readByte(data);
		final byte b8 = readByte(data);
		return (((long)b1 & 0xff)) | (((long)b2 & 0xff) << 8) | (((long)b3 & 0xff) << 16) | (((long)b4 & 0xff) << 24) | (((long)b5 & 0xff) << 32)
				| (((long)b6 & 0xff) << 40) | (((long)b7 & 0xff) << 48) | (((long)b8 & 0xff) << 56);
	}

	protected static byte readByte(ChannelDataBuffer data) throws IOException
	{
		return (byte)data.getInputStream().read();
	}

	protected static long readLong(ChannelDataBuffer data) throws IOException
	{
		int shift = 0;
		long result = 0;
		while(shift < 64)
		{
			final byte b = readByte(data);
			result |= (long)(b & 0x7F) << shift;
			if((b & 0x80) == 0)
			{
				return result;
			}
			shift += 7;
		}
		throw new IOException("encountered a malformed varint");
	}

	protected static short readShort(ChannelDataBuffer data) throws IOException
	{
		int shift = 0;
		short result = 0;
		while(shift < 16)
		{
			final byte b = readByte(data);
			result |= (short)(b & 0x7F) << shift;
			if((b & 0x80) == 0)
			{
				return result;
			}
			shift += 7;
		}
		throw new IOException("encountered a malformed varint");
	}

	protected static char readChar(ChannelDataBuffer data) throws IOException
	{
		int shift = 0;
		char result = 0;
		while(shift < 16)
		{
			final byte b = readByte(data);
			result |= (short)(b & 0x7F) << shift;
			if((b & 0x80) == 0)
			{
				return result;
			}
			shift += 7;
		}
		throw new IOException("encountered a malformed varint");
	}


	public static int readInt(ChannelDataBuffer data) throws IOException
	{
		byte tmp = readByte(data);
		if(tmp >= 0)
		{
			return tmp;
		}
		int result = tmp & 0x7f;
		if((tmp = readByte(data)) >= 0)
		{
			result |= tmp << 7;
		}
		else
		{
			result |= (tmp & 0x7f) << 7;
			if((tmp = readByte(data)) >= 0)
			{
				result |= tmp << 14;
			}
			else
			{
				result |= (tmp & 0x7f) << 14;
				if((tmp = readByte(data)) >= 0)
				{
					result |= tmp << 21;
				}
				else
				{
					result |= (tmp & 0x7f) << 21;
					result |= (tmp = readByte(data)) << 28;
					if(tmp < 0)
					{
						// Discard upper 32 bits.
						for(int i = 0; i < 5; i++)
						{
							if(readByte(data) >= 0)
							{
								return result;
							}
						}
						throw new IOException("encountered a malformed varint");
					}
				}
			}
		}
		return result;
	}

	protected static boolean readBool(ChannelDataBuffer data) throws IOException
	{
		return readInt(data) != 0;
	}

	protected static byte[] readBytes(ChannelDataBuffer data) throws IOException
	{
		int size = readInt(data);
		if(size > 0 && data.getInputStream().available() >= size)
		{
			byte[] ret = new byte[size];
			data.getInputStream().read(ret);
			return ret;
		}
		else
		{
			throw new IOException("No enoght data in stream!");
		}
	}
	
	protected static void readBytes(ChannelDataBuffer data, byte[] content) throws IOException
	{
		readBytes(data, content, 0, content.length);
	}
	
	protected static void readBytes(ChannelDataBuffer data, byte[] content, int off, int len) throws IOException
	{
		int size = readInt(data);
		if(size > 0 && data.getInputStream().available() >= size && len == size)
		{
			data.getInputStream().read(content, off, len);
		}
		else
		{
			throw new IOException("No enoght data in stream!");
		}
	}

	protected static double readDouble(ChannelDataBuffer data) throws IOException
	{
		return Double.longBitsToDouble(readRawLittleEndian64(data));
	}

	protected static float readFloat(ChannelDataBuffer data) throws IOException
	{
		return Float.intBitsToFloat(readRawLittleEndian32(data));
	}

	protected static int readTag(ChannelDataBuffer data) throws IOException
	{
		if(data.getInputStream().available() == 0)
		{
			return 0;
		}
		int tag = readInt(data);
		return tag >>> 3;
	}


	protected static String readString(ChannelDataBuffer data) throws IOException
	{
		int size = readInt(data);
		if(data.getInputStream().available() >= size)
		{
			byte[] buf = new byte[size];
			data.getInputStream().read(buf);
			String s = new String(buf, 0, size, "UTF-8");
			//data.position(data.position() + size);
			return s;
		}
		else
		{
			throw new IOException("No enought data in stream!" + data.getInputStream().available() +"-" + size);
		}
		
	}

	protected static void writeByte(ChannelDataBuffer data, final byte value) throws IOException
	{
		data.getOutputStream().write(value);
	}

	protected static void writeBytes(ChannelDataBuffer data, final byte[] value, int off, int len) throws IOException
	{
		data.getOutputStream().write(value, off, len);
	}

	protected static void writeBytes(ChannelDataBuffer data, final byte[] value) throws IOException
	{
		data.getOutputStream().write(value);
	}

	protected static void writeByte(ChannelDataBuffer data, final int value) throws IOException
	{
		writeByte(data, (byte)value);
	}

	protected static void writeBoolean(ChannelDataBuffer data, boolean value) throws IOException
	{
		writeByte(data, value ? 1 : 0);
	}

	protected static void write(ChannelDataBuffer data, int value) throws IOException
	{
		writeInt(data, value);
	}

	public static void writeInt(ChannelDataBuffer data, int value) throws IOException
	{
		if(value >= 0)
		{
			while(true)
			{
				if((value & ~0x7F) == 0)
				{
					writeByte(data, (byte)value);
					return;
				}
				else
				{
					writeByte(data, (value & 0x7F) | 0x80);
					value >>>= 7;
				}
			}
		}
		else
		{
			writeLong(data, value);
		}

	}

	protected static void writeShort(ChannelDataBuffer data, int value) throws IOException
	{
		writeShort(data, (short)value);
	}

	protected static void writeShort(ChannelDataBuffer data, short value) throws IOException
	{
		if(value >= 0)
		{
			while(true)
			{
				if((value & ~0x7FL) == 0)
				{
					writeByte(data, (int)value);
					return;
				}
				else
				{
					writeByte(data, ((int)value & 0x7F) | 0x80);
					value >>>= 7;
				}
			}
		}
		else
		{
			writeInt(data, value);
		}
	}

	protected static void writeChar(ChannelDataBuffer data, int value) throws IOException
	{
		writeChar(data, (char)value);
	}

	protected static void writeChar(ChannelDataBuffer data, char value) throws IOException
	{
		writeShort(data, value);
	}

	protected static void writeLong(ChannelDataBuffer data, long value) throws IOException
	{
		while(true)
		{
			if((value & ~0x7FL) == 0)
			{
				writeByte(data, (int)value);
				return;
			}
			else
			{
				writeByte(data, ((int)value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

	protected static void writeRawLittleEndian32(ChannelDataBuffer data, final int value) throws IOException
	{
		writeByte(data, (value) & 0xFF);
		writeByte(data, (value >> 8) & 0xFF);
		writeByte(data, (value >> 16) & 0xFF);
		writeByte(data, (value >> 24) & 0xFF);
	}

	protected static void writeRawLittleEndian64(ChannelDataBuffer data, final long value) throws IOException
	{
		writeByte(data, (int)(value) & 0xFF);
		writeByte(data, (int)(value >> 8) & 0xFF);
		writeByte(data, (int)(value >> 16) & 0xFF);
		writeByte(data, (int)(value >> 24) & 0xFF);
		writeByte(data, (int)(value >> 32) & 0xFF);
		writeByte(data, (int)(value >> 40) & 0xFF);
		writeByte(data, (int)(value >> 48) & 0xFF);
		writeByte(data, (int)(value >> 56) & 0xFF);
	}

	protected static void writeFloat(ChannelDataBuffer data, final float value) throws IOException
	{
		writeRawLittleEndian32(data, Float.floatToRawIntBits(value));
	}

	protected static void writeDouble(ChannelDataBuffer data, final double value) throws IOException
	{
		writeRawLittleEndian64(data, Double.doubleToRawLongBits(value));
	}

	protected static void writeTag(ChannelDataBuffer data, int tag) throws IOException
	{
		tag = ((tag << 3) | 0);
		write(data, tag);
	}

	protected static void write(ChannelDataBuffer data, byte[] b) throws IOException
	{
		// buffer.put(b);
		data.getOutputStream().write(b);
	}

	protected static void writeString(ChannelDataBuffer data, String s) throws IOException
	{
		if(null == s)
			return;
		final byte[] bytes = s.getBytes("UTF-8");
		writeInt(data, bytes.length);
		write(data, bytes);
	}

	protected static <T> T readObject(ChannelDataBuffer data, Class<T> type) throws IOException
	{
		//System.out.println("######" + type.getName());
		Type dataType = ReflectionCache.getType(type);
		switch (dataType) 
		{
		   case BOOL:
		   {
			   Boolean value = readBool(data);
			   return (T) value;
		   }
		   case BYTE:
		   {
			  Byte value = readByte(data);
			  return (T) value;
		   }
		   case INT:
		   {
			  Integer value = readInt(data);
			  return (T) value;
		   }
		   case FLOAT:
		   {
			   Float value = readFloat(data);
			   return (T) value;
		   }
		   case LONG:
		   {
			   Long value = readLong(data);
			   return (T) value;
		   }
		   case DOUBLE:
		   {
			   Double value = readDouble(data);
			   return (T) value;
		   }
		   case STRING:
		   {
			   String value = readString(data);
			   return (T) value;
		   }
		   case SHORT:
		   {
			   Short value = readShort(data);
			   return (T) value;
		   }
		   case CHAR:
		   {
			   Character value = readChar(data);
			   return (T) value;
		   }
		}
		int indicator = readTag(data);
		try
		{
			switch(indicator)
			{
				case INDICATOR_TYPE:
				{
					type = (Class<T>)Class.forName(readString(data), true, ContextUtil.getDeserializeClassLoader());
					dataType = ReflectionCache.getType(type);
					break;
				}
				case INDICATOR_PROXY:
				{
					dataType = Type.PROXY;
					break;
				}
				case INDICATOR_OTHER:
				{
					dataType = Type.OTHER;
					break;
				}
				case INDICATOR_LOOP_REF:
				{
					int refSeq = readInt(data);
					return (T)ContextUtil.getDeserializeThreadLocalObject(refSeq);
				}
				case INDICATOR_NULL:
				{
					return null;
				}
				default:
					break;
			}
			if(data.getInputStream().available() <= 0)
			{
				return null;
			}
			return (T)SerailizerStreamFactory.getSerializer(dataType).unmarshal(type, data);
		}
		catch(IOException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new IOException(e.getMessage());
		}

	}

	protected static void writeObject(ChannelDataBuffer data, Object value, Class type) throws IOException
	{
		Type declType = ReflectionCache.getType(type);
		switch (declType) 
		{
		   case BOOL:
		   {
			   writeBoolean(data, (Boolean) value);
			   return ;
		   }
		   case BYTE:
		   {
			  writeByte(data, (Byte) value);
			  return ;
		   }
		   case INT:
		   {
			  writeInt(data, (Integer) value);
			  return ;
		   }
		   case FLOAT:
		   {
			   writeFloat(data, (Float) value);
			   return ;
		   }
		   case LONG:
		   {
			   writeLong(data, (Long) value);
			   return;
		   }
		   case DOUBLE:
		   {
			   writeDouble(data, (Double) value);
			   return;
		   }
		   case STRING:
		   {
			   writeString(data, (String) value);
			   return;
		   }
		   case SHORT:
		   {
			   writeShort(data, (Short) value);
			   return;
		   }
		   case CHAR:
		   {
			   writeChar(data, (Character) value);
			   return;
		   }
		}
		
		Class clazz = null != value?value.getClass():type;
		Type dataType = ReflectionCache.getType(clazz);
		if(null == value)
		{
			dataType = Type.NULL;
		}
		// loop reference
		int refSeq = null != value?ContextUtil.querySerializeThreadLocalObjectIndex(value):-1;
		if(refSeq != -1)
		{
			writeTag(data, INDICATOR_LOOP_REF);
			writeInt(data, refSeq); 
			return;
		}
		
		switch(dataType)
		{
			case PROXY:
			{
				writeTag(data, INDICATOR_PROXY);
				break;
			}
			case OTHER:
			{
				writeTag(data, INDICATOR_OTHER);
				break;
			}
			case NULL:
			{
				writeTag(data, INDICATOR_NULL);
				return;
			}
			default:
			{
				if(ClassUtil.equals(clazz, type))
				{
					writeTag(data, INDICATOR_DEFAULT);
				}
				else
				{
					writeTag(data, INDICATOR_TYPE);
					writeString(data, clazz.getName());
				}
				dataType = ReflectionCache.getType(clazz);
				break;
			}
		}
		switch(dataType)
		{
			case POJO:
			case PROXY:
			case OTHER:
			case ARRAY:
			{
				ContextUtil.addSerializeThreadLocalObject(value);
				break;
			}
		}
		SerailizerStream stream = SerailizerStreamFactory.getSerializer(dataType);
		stream.marshal(value, data);
	}

	protected static void writeObject(ChannelDataBuffer data, Object value) throws IOException
	{
		if(null != value)
		{
			writeObject(data, value, value.getClass());
		}
	}
	
	public static ChannelDataBuffer serialize(Object obj, ChannelDataBuffer data) throws NotSerializableException, IOException
	{
		writeObject(data, obj);
		return data;
	}
	
	public static <T> T deserialize(Class<T> type, ChannelDataBuffer data) throws NotSerializableException, IOException,InstantiationException
	{
		return readObject(data, type);
	}
	
	protected abstract ChannelDataBuffer marshal(T obj, ChannelDataBuffer data) throws NotSerializableException, IOException;

	protected abstract T unmarshal(Class<T> type, ChannelDataBuffer data) throws NotSerializableException, IOException, InstantiationException;
	
}
