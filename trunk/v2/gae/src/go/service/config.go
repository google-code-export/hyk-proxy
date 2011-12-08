package service

import (
	"appengine"
	"appengine/datastore"
	"appengine/memcache"
	"event"
	"bytes"
	"codec"
	"strconv"
	"strings"
)


func initServerConfig() *event.GAEServerConfig {
	cfg := new(event.GAEServerConfig)
	cfg.RetryFetchCount = 2
	cfg.RangeFetchLimit = 256 * 1024
	cfg.MaxXMPPDataPackageSize = 40960
	cfg.CompressType = C_SNAPPY
	cfg.EncryptType = E_SE1
	cfg.CompressFilter = make(map[string]string)
}

var ServerConfig = initServerConfig()

func toPropertyList() datastore.PropertyList {
	var ret = make([]datastore.PropertyList, 0, 6)
	ret = append(ret, datastore.Property{
		Name:  "RetryFetchCount",
		Value: Itoa(ServerConfig.RetryFetchCount),
	})
	ret = append(ret, datastore.Property{
		Name:  "RangeFetchLimit",
		Value: Itoa(ServerConfig.RangeFetchLimit),
	})
	ret = append(ret, datastore.Property{
		Name:  "MaxXMPPDataPackageSize",
		Value: Itoa(ServerConfig.MaxXMPPDataPackageSize),
	})
	ret = append(ret, datastore.Property{
		Name:  "CompressType",
		Value: Itoa(ServerConfig.CompressType),
	})
	ret = append(ret, datastore.Property{
		Name:  "EncryptType",
		Value: Itoa(ServerConfig.EncryptType),
	})
	var tmp string
	for key, _ := range ServerConfig.CompressFilter {
		tmp += key
		tmp += ";"
	}
	ret = append(ret, datastore.Property{
		Name:  "CompressFilter",
		Value: tmp,
	})
	return ret
}

func fromPropertyList(item datastore.PropertyList) {
	for v := range item {
		switch v.Name {
		case "RetryFetchCount":
			ServerConfig.RetryFetchCount = uint32(Atoui64(v.Value.(string)))
		case "RangeFetchLimit":
			ServerConfig.RangeFetchLimit = uint32(Atoui64(v.Value.(string)))
		case "MaxXMPPDataPackageSize":
			ServerConfig.MaxXMPPDataPackageSize = uint32(Atoui64(v.Value.(string)))
		case "CompressType":
			ServerConfig.CompressType = uint32(Atoui64(v.Value.(string)))
		case "EncryptType":
			ServerConfig.EncryptType = uint32(Atoui64(v.Value.(string)))
		case "CompressFilter":
			str := v.Value.(string)
			ss := strings.Split(str)
			for s := range ss {
				s = strings.TrimSpace(s)
				if len(s) > 0 {
					ServerConfig.CompressFilter[s] = s
				}
			}
		}
	}
}

func SaveServerConfig(ctx appengine.Context) {
	key := datastore.NewKey(ctx, "ServerConfig", nil, 1, nil)
	item := toPropertyList()
	_, err := datastore.Put(ctx, key, item)
	if err != nil {
		return
	}
	var buf bytes.Buffer
	ServerConfig.Encode(&buf)
	memitem := &memcache.Item{
		Key:   "ServerConfig:",
		Value: buf.Bytes(),
	}
	// Add the item to the memcache, if the key does not already exist
	memcache.Set(ctx, memitem)
}

func LoadServerConfig(ctx appengine.Context) {
	if item, err := memcache.Get(ctx, "ServerConfig:"); err == nil {
		buf := bytes.NewBuffer(item.Value)
		if ServerConfig.Decode(buf) {
			return
		}
	}
	var item datastore.PropertyList
	key := datastore.NewKey(ctx, "ServerConfig", nil, 1, nil)
	if err := datastore.Get(ctx, key, item); err != nil && err != datastore.ErrNoSuchEntity {
		return
	}
	fromPropertyList(item)
	var buf bytes.Buffer
	ServerConfig.Encode(&buf)
	memitem := &memcache.Item{
		Key:   "ServerConfig:",
		Value: buf.Bytes(),
	}
	memcache.Set(ctx, memitem)
}
