/**
 * 
 */
package com.hyk.serializer.io;

/**
 * @author qiying.wang
 *
 */
public enum Type {
	BOOL(0), BYTE(1), INT(2), LONG(3), FLOAT(4), DOUBLE(5), SHORT(6), CHAR(
			7), STRING(8), ARRAY(9), POJO(10), ENUM(11), PROXY(12),OTHER(13), NULL(14);

	private int value;

	private Type(int value) {
		this.value = value;
	}

	public final int getValue() {
		return value;
	}
}
