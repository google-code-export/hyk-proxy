package se1

import (
	"bytes"
)

func Encrypt(buffer *bytes.Buffer) *bytes.Buffer {
	buf := buffer.Bytes()
	for i := 0; i < len(buf); i++ {
		var k int32 = int32(buf[i])
		v := k - 1
		if v < 0 {
			v += 256
			buf[i] = uint8(v)
		}
	}
	return bytes.NewBuffer(buf)
}

func Decrypt(buffer *bytes.Buffer) *bytes.Buffer {
	buf := buffer.Bytes()
	for i := 0; i < len(buf); i++ {
		var k int32 = int32(buf[i])
		v := k + 1
		if v > 256 {
			v -= 256
			buf[i] = uint8(v)
		}
	}
	return bytes.NewBuffer(buf)
}
