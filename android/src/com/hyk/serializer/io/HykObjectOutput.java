/**
 * 
 */
package com.hyk.serializer.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.hyk.serializer.reflect.ReflectionCache;

/**
 * @author qiying.wang
 * 
 */
public class HykObjectOutput<T> implements ObjectOutput {
	OutputStream stream;

	public HykObjectOutput(OutputStream stream) {
		this.stream = stream;
	}

	public void close() throws IOException {
		stream.close();
	}

	public void flush() throws IOException {
		stream.flush();
	}

	public void write(byte[] b) throws IOException {
		// buffer.put(b);
		stream.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		// buffer.put(b, off, len);
		stream.write(b, off, len);
	}

	
	public void writeObject(Object obj) throws IOException {
		if (null == obj)
			return;
		writeObject(obj, obj.getClass());
	}
	
	public void writeObject(Object obj, Class declClass) throws IOException {
		if (null == obj)
		{
			return;
		}
			
		try {
			Class clazz = obj.getClass();
			Type t = ReflectionCache.getType(clazz);
			switch (t) {
			case BOOL: {
				writeBoolean((Boolean) obj);
				break;
			}
			case BYTE: {
				writeByte((Byte) obj);
				break;
			}
			case CHAR: {
				writeChar((Character) obj);
				break;
			}
			case SHORT: {
				writeShort((Character) obj);
				break;
			}
			case FLOAT: {
				writeFloat((Float) obj);
				break;
			}
			case DOUBLE: {
				writeDouble((Double) obj);
				break;
			}
			case INT: {
				writeInt((Integer) obj);
				break;
			}
			case LONG: {
				writeLong((Long) obj);
				break;
			}
			case STRING: {
				writeUTF((String) obj);
				break;
			}
			case ENUM:
			{
				Enum e = (Enum) obj;
				writeUTF(e.name());
				break;
			}
			case PROXY:
			{
				Class[] interfaces = clazz.getInterfaces();
				String[] interfaceNames = new String[interfaces.length];
				for (int i = 0; i < interfaceNames.length; i++) {
					interfaceNames[i] = interfaces[i].getName();
				}
				writeObject(interfaceNames);
				InvocationHandler handler = Proxy.getInvocationHandler(obj);
				writeUTF(handler.getClass().getName());
				writeObject(handler);
				break;
			}
			case POJO: {
				
				if (!(obj instanceof Serializable)) {
					throw new NotSerializableException(clazz.getName());
				}
				if(clazz.equals(declClass))
				{
					writeInt(0);
				}
				else
				{
					writeInt(1);
					writeUTF(obj.getClass().getName());
				}			
				if (obj instanceof Externalizable) {
					Externalizable externalizable = (Externalizable) obj;
					externalizable.writeExternal(this);
					return;
				}					
				
				Field[] fs = ReflectionCache.getSerializableFields(clazz);
				for (int i = 0; i < fs.length; i++) {
					Field f = fs[i];		
					Object fieldValue = f.get(obj);
					if(null != fieldValue)
					{
						writeTag(i + 1);
						writeObject(fieldValue, f.getType());
					}		
				}
				writeTag(0);
				break;
			}
			case ARRAY: {
				int len = Array.getLength(obj);
				writeInt(len);
				int index = 0;
				while (index < len) {
					writeObject(Array.get(obj, index));
					index++;
				}
				break;
			}
			default:
				break;
			}
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	public void writeBytes(String s) throws IOException {
		if (null == s)
			return;
		final byte[] bytes = s.getBytes("UTF-8");
		writeInt(bytes.length);
		write(bytes);
	}

	public void writeChars(String s) throws IOException {
		// TODO Auto-generated method stub
		writeBytes(s);
//		if (null == s)
//			return;
//		writeInt(s.length());
//		char[] buf = s.toCharArray();
//		for (int i = 0; i < buf.length; i++) {
//			writeChar(buf[i]);
//		}
	}

	public void writeByte(final byte value) throws IOException {
		stream.write(value);
	}

	public void writeByte(final int value) throws IOException {
		writeByte((byte) value);
	}

	public void writeBoolean(boolean value) throws IOException {
		writeByte(value ? 1 : 0);
	}

	public void write(int value) throws IOException {
		writeInt(value);
	}

	public void writeInt(int value) throws IOException {
		if (value >= 0) {
			while (true) {
				if ((value & ~0x7F) == 0) {
					writeByte((byte) value);
					return;
				} else {
					writeByte((value & 0x7F) | 0x80);
					value >>>= 7;
				}
			}
		} else {
			writeLong(value);
		}

	}

	public void writeShort(int value) throws IOException {
		writeShort((short) value);
	}

	public void writeShort(short value) throws IOException {
		if (value >= 0) {
			while (true) {
				if ((value & ~0x7FL) == 0) {
					writeByte((int) value);
					return;
				} else {
					writeByte(((int) value & 0x7F) | 0x80);
					value >>>= 7;
				}
			}
		} else {
			writeInt(value);
		}
	}

	public void writeChar(int value) throws IOException {
		writeChar((char) value);
	}

	public void writeChar(char value) throws IOException {
		writeShort(value);
	}

	public void writeLong(long value) throws IOException {
		while (true) {
			if ((value & ~0x7FL) == 0) {
				writeByte((int) value);
				return;
			} else {
				writeByte(((int) value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

	public void writeRawLittleEndian32(final int value) throws IOException {
		writeByte((value) & 0xFF);
		writeByte((value >> 8) & 0xFF);
		writeByte((value >> 16) & 0xFF);
		writeByte((value >> 24) & 0xFF);
	}

	public void writeRawLittleEndian64(final long value) throws IOException {
		writeByte((int) (value) & 0xFF);
		writeByte((int) (value >> 8) & 0xFF);
		writeByte((int) (value >> 16) & 0xFF);
		writeByte((int) (value >> 24) & 0xFF);
		writeByte((int) (value >> 32) & 0xFF);
		writeByte((int) (value >> 40) & 0xFF);
		writeByte((int) (value >> 48) & 0xFF);
		writeByte((int) (value >> 56) & 0xFF);
	}

	public void writeFloat(final float value) throws IOException {
		writeRawLittleEndian32(Float.floatToRawIntBits(value));
	}

	public void writeDouble(final double value) throws IOException {
		writeRawLittleEndian64(Double.doubleToRawLongBits(value));
	}

	public void writeTag(int tag) throws IOException {
		tag = ((tag << 3) | 0);
		write(tag);
	}

	public void writeUTF(String s) throws IOException {
		writeChars(s);
	}
}
