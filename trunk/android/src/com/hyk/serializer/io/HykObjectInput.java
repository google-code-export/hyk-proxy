/**
 * 
 */
package com.hyk.serializer.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.reflect.ReflectionCache;


/**
 * @author qiying.wang
 *
 */
public class HykObjectInput implements ObjectInput {

	//InputStream is;
	BufferedInputStream is;
	Class clazz;
	HykSerializer serializer;

	public HykObjectInput(BufferedInputStream is, HykSerializer serializer)
	{
		this.is = is;
		this.serializer = serializer;
	}
	
	public int readRawLittleEndian32() throws IOException {
		final byte b1 = readByte();
		final byte b2 = readByte();
		final byte b3 = readByte();
		final byte b4 = readByte();
		return (((int) b1 & 0xff)) | (((int) b2 & 0xff) << 8)
				| (((int) b3 & 0xff) << 16) | (((int) b4 & 0xff) << 24);
	}

	public long readRawLittleEndian64() throws IOException {
		final byte b1 = readByte();
		final byte b2 = readByte();
		final byte b3 = readByte();
		final byte b4 = readByte();
		final byte b5 = readByte();
		final byte b6 = readByte();
		final byte b7 = readByte();
		final byte b8 = readByte();
		return (((long) b1 & 0xff)) | (((long) b2 & 0xff) << 8)
				| (((long) b3 & 0xff) << 16) | (((long) b4 & 0xff) << 24)
				| (((long) b5 & 0xff) << 32) | (((long) b6 & 0xff) << 40)
				| (((long) b7 & 0xff) << 48) | (((long) b8 & 0xff) << 56);
	}


	public int available() throws IOException {
		return is.available();
	}


	public void close() throws IOException {
		is.close();       
	}

	
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return readByte();
	}

	
	public int read(byte[] b) throws IOException {
		return is.read(b);
	}

	
	public int read(byte[] b, int off, int len) throws IOException {
		return is.read(b, off, len);
	}

	private <T> T readRawObject(Class<T> clazz) throws ClassNotFoundException, IOException
	{
		int indicator = readInt();
		if(indicator == 1)
		{
			Class realClass = Class.forName(readUTF(), true, clazz.getClassLoader());
			return (T) readObject(realClass);
		}
		return readObject(clazz);
	}
	
	public <T> T readObject(Class<T> clazz) throws IOException {
		try {
			if(available() == 0) return null;
			Type t = ReflectionCache.getType(clazz);
			switch (t) {
			case BOOL: {
				Boolean b = readBool();
				return (T) b;
			}
			case BYTE: {
				Byte b = readByte();
				return (T) b;
			}
			case CHAR: {
				Character c = readChar();
				return (T) c;
			}
			case SHORT: {
				Short s = readShort();
				return (T) s;
			}
			case FLOAT: {
				Float f = readFloat();
				return (T) f;
			}
			case DOUBLE: {
				Double d = readDouble();
				return (T) d;
			}
			case INT: {
				Integer i = readInt();
				return (T) i;
			}
			case LONG: {
				Long l = readLong();
				return (T) l;
			}
			case STRING: {
				String s = readUTF();
				return (T) s;
			}
			case ENUM:
			{
				String name = readUTF();
				Class cls = clazz;
				return (T) Enum.valueOf(cls, name);
			}
			case PROXY:
			{			
				ClassLoader loader = clazz.getClassLoader();
				String[] interfaceNames = readObject(String[].class);
				Class[] interfaces = new Class[interfaceNames.length];
				for (int i = 0; i < interfaceNames.length; i++) {
					interfaces[i] = Class.forName(interfaceNames[i], true, loader);
				}
				Class proxyHandlerClass = Class.forName(readUTF(), true, loader);
				InvocationHandler handler = (InvocationHandler) readObject(proxyHandlerClass);
				return (T) Proxy.newProxyInstance(loader, interfaces, handler);
			}
			case POJO: {
				int indicator = readInt();
				Class realClass = clazz;
				if(indicator == 1)
				{
					realClass =  Class.forName(readUTF());
				}
				Constructor<T> cons = ReflectionCache.getDefaultConstructor(realClass);
				T ret = cons.newInstance();
				if (!(ret instanceof Serializable)) {
					throw new NotSerializableException(realClass.getName());
				}
				if (ret instanceof Externalizable) {
					Externalizable externalizable = (Externalizable) ret;
					externalizable.readExternal(this);
					return ret;
				}
				Field[] fs = ReflectionCache.getSerializableFields(realClass);
				while (true) {
					int tag = readTag();
					if (tag == 0)
						break;
					Field f = fs[tag - 1];
					Class fieldType = f.getType();
					//System.out.println("####" + f.getName());
					f.set(ret, readObject(fieldType));
				}
				return ret;
			}
			case ARRAY: {
				int len = readInt();
				int index = 0;
				Object array = Array.newInstance(clazz.getComponentType(), len);
				while (index < len) {
					Array.set(array, index, readObject(clazz.getComponentType()));
					index++;
				}
				return (T) array;
			}
			default: {
				throw new IOException("Unsupport type:" + t);
			}
			}

		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

	}
	public Object readObject() throws IOException {
		
		throw new IOException("Not support in class 'HykObjectInput', please use HykObjectInput.readObject(Class clazz) instead!");
	}

	
	public long skip(long n) throws IOException {
		return is.skip(n);
	}

	
	public boolean readBoolean() throws IOException {
		// TODO Auto-generated method stub
		return readInt() != 0;
	}

	
	public void readFully(byte[] b) throws IOException {
		is.read(b);
	}

	
	public void readFully(byte[] b, int off, int len) throws IOException {
		is.read(b, off, len);
	}

	
	public String readLine() throws IOException {
		// TODO Auto-generated method stub
		return readUTF();
	}

	public byte readByte() throws IOException {
		return (byte) is.read();
	}

	public long readLong() throws IOException {
		int shift = 0;
		long result = 0;
		while (shift < 64) {
			final byte b = readByte();
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return result;
			}
			shift += 7;
		}
		throw new IOException("encountered a malformed varint");
	}

	public short readShort() throws IOException {
		int shift = 0;
		short result = 0;
		while (shift < 16) {
			final byte b = readByte();
			result |= (short) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return result;
			}
			shift += 7;
		}
		throw new IOException("encountered a malformed varint");
	}

	public char readChar() throws IOException {
		int shift = 0;
		char result = 0;
		while (shift < 16) {
			final byte b = readByte();
			result |= (short) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return result;
			}
			shift += 7;
		}
		throw new IOException("encountered a malformed varint");
	}

	public int readInt() throws IOException {
		byte tmp = readByte();
		if (tmp >= 0) {
			return tmp;
		}
		int result = tmp & 0x7f;
		if ((tmp = readByte()) >= 0) {
			result |= tmp << 7;
		} else {
			result |= (tmp & 0x7f) << 7;
			if ((tmp = readByte()) >= 0) {
				result |= tmp << 14;
			} else {
				result |= (tmp & 0x7f) << 14;
				if ((tmp = readByte()) >= 0) {
					result |= tmp << 21;
				} else {
					result |= (tmp & 0x7f) << 21;
					result |= (tmp = readByte()) << 28;
					if (tmp < 0) {
						// Discard upper 32 bits.
						for (int i = 0; i < 5; i++) {
							if (readByte() >= 0) {
								return result;
							}
						}
						throw new IOException(
								"encountered a malformed varint");
					}
				}
			}
		}
		return result;
	}

	public boolean readBool() throws IOException {
		return readInt() != 0;
	}

	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readRawLittleEndian64());
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readRawLittleEndian32());
	}

	
	public String readUTF() throws IOException {
		int size = readInt();
//		char[] buf = new char[size];
//		for (int i = 0; i < buf.length; i++) {
//			buf[i] = readChar();
//		}
//		return new String(buf);
		
		return is.readString(size);

	}

	
	public int readUnsignedByte() throws IOException {
		// TODO Auto-generated method stub
		return read();
	}


	public int readUnsignedShort() throws IOException {	
		return readInt();
	}

	public int skipBytes(int n) throws IOException {
		return (int) is.skip(n);
	}
	
	public int readTag()throws IOException 
	{
		if(is.available() == 0)
		{
			return 0;
		}
		int tag = readInt();
		return tag >>> 3;
	}

}
