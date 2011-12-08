package event

import (
	"bytes"
	"encoding/binary"
	"codec"
	"os"
)

type GAEServerConfig struct {
	RetryFetchCount        uint32
	MaxXMPPDataPackageSize uint32
	RangeFetchLimit        uint32
	CompressType           uint32
	EncryptType            uint32
	CompressFilter         map[string]string
}

func (cfg *GAEServerConfig) Encode(buffer *bytes.Buffer) bool {
	codec.WriteUvarint(buffer, cfg.RetryFetchCount)
	codec.WriteUvarint(buffer, cfg.MaxXMPPDataPackageSize)
	codec.WriteUvarint(buffer, cfg.RangeFetchLimit)
	codec.WriteUvarint(buffer, cfg.CompressType)
	codec.WriteUvarint(buffer, cfg.EncryptType)
	codec.WriteUvarint(buffer, len(cfg.CompressFilter))
	for key = range cfg.CompressFilter {
		codec.WriteVarString(buffer, key)
	}
	return true
}

func (cfg *GAEServerConfig) Decode(buffer *bytes.Buffer) bool {
	tmp1, err1 := codec.ReadUvarint(buffer)
	tmp2, err2 := codec.ReadUvarint(buffer)
	tmp3, err3 := codec.ReadUvarint(buffer)
	tmp4, err4 := codec.ReadUvarint(buffer)
	tmp5, err5 := codec.ReadUvarint(buffer)
	tmp6, err6 := codec.ReadUvarint(buffer)
	if err1 != nil || err2 != nil || err3 != nil || err4 != nil || err5 != nil || err6 != nil {
		return false
	}
	cfg.RetryFetchCount = uint32(tmp1)
	cfg.MaxXMPPDataPackageSize = uint32(tmp2)
	cfg.RangeFetchLimit = uint32(tmp3)
	cfg.CompressType = uint32(tmp4)
	cfg.EncryptType = uint32(tmp5)
	filter := make(map[string]string)
	for i := 0; i < int(tmp6); i++ {
		line, ok := codec.ReadVarString(buffer)
		if !ok {
			return false
		}
		cfg.CompressFilter[line] = line
	}
	cfg.CompressFilter = filter
	return true
}

type User struct {
	Email     string
	Passwd    string
	Group     string
	AuthToken string
	BlackList map[string]string
}

func (cfg *User) Encode(buffer *bytes.Buffer) bool {
}
func (cfg *User) Decode(buffer *bytes.Buffer) bool {
}

type Group struct {
	Name      string
	BlackList map[string]string
}

func (cfg *Group) Encode(buffer *bytes.Buffer) bool {
}
func (cfg *Group) Decode(buffer *bytes.Buffer) bool {
}
