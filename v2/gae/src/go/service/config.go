package service

import (
	"appengine"
	"appengine/datastore"
	"appengine/memcache"
	"event"
	"bytes"
	//"codec"
	"strconv"
	"strings"
)

func initServerConfig() *event.GAEServerConfig {
	cfg := new(event.GAEServerConfig)
	cfg.RetryFetchCount = 2
	cfg.RangeFetchLimit = 256 * 1024
	cfg.MaxXMPPDataPackageSize = 40960
	cfg.CompressType = event.C_SNAPPY
	cfg.EncryptType = event.E_SE1
	cfg.CompressFilter = make(map[string]string)
	return cfg
}

var ServerConfig = initServerConfig()

func toPropertyList() datastore.PropertyList {
	var ret = make(datastore.PropertyList, 0, 6)
	ret = append(ret, datastore.Property{
		Name:  "RetryFetchCount",
		Value: strconv.Itoa64(int64(ServerConfig.RetryFetchCount)),
	})
	ret = append(ret, datastore.Property{
		Name:  "RangeFetchLimit",
		Value: strconv.Itoa64(int64(ServerConfig.RangeFetchLimit)),
	})
	ret = append(ret, datastore.Property{
		Name:  "MaxXMPPDataPackageSize",
		Value: strconv.Itoa64(int64(ServerConfig.MaxXMPPDataPackageSize)),
	})
	ret = append(ret, datastore.Property{
		Name:  "CompressType",
		Value: strconv.Itoa64(int64(ServerConfig.CompressType)),
	})
	ret = append(ret, datastore.Property{
		Name:  "EncryptType",
		Value: strconv.Itoa64(int64(ServerConfig.EncryptType)),
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
	for _,v := range item {
		switch v.Name {
		case "RetryFetchCount":
			tmp, _ := strconv.Atoui64(v.Value.(string))
			ServerConfig.RetryFetchCount = uint32(tmp)
		case "RangeFetchLimit":
			tmp, _ := strconv.Atoui64(v.Value.(string))
			ServerConfig.RangeFetchLimit= uint32(tmp)
		case "MaxXMPPDataPackageSize":
			tmp, _ := strconv.Atoui64(v.Value.(string))
			ServerConfig.MaxXMPPDataPackageSize = uint32(tmp)
		case "CompressType":
			tmp, _ := strconv.Atoui64(v.Value.(string))
			ServerConfig.CompressType = uint32(tmp)
		case "EncryptType":
			tmp, _ := strconv.Atoui64(v.Value.(string))
			ServerConfig.EncryptType = uint32(tmp)
		case "CompressFilter":
			str := v.Value.(string)
			ss := strings.Split(str, ";")
			for _,s := range ss {
				s = strings.TrimSpace(s)
				if len(s) > 0 {
					ServerConfig.CompressFilter[s] = s
				}
			}
		}
	}
}

func SaveServerConfig(ctx appengine.Context) {
	key := datastore.NewKey(ctx, "ServerConfig", "", 1, nil)
	item := toPropertyList()
	_, err := datastore.Put(ctx, key, &item)
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
	key := datastore.NewKey(ctx, "ServerConfig", "", 1, nil)
	if err := datastore.Get(ctx, key, &item); err != nil {
	    SaveServerConfig(ctx)
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

func HandlerConfigEvent(ctx appengine.Context,ev *event.ServerConfigEvent)event.Event{
   switch ev.Operation{
      case event.GET_CONFIG_REQ:
	     res := new(event.ServerConfigEvent)
		 res.Operation = event.GET_CONFIG_RES
		 res.Cfg = ServerConfig
		 return res
	  case  event.SET_CONFIG_REQ:
	     if nil != ev.Cfg{
		    ServerConfig = ev.Cfg
			SaveServerConfig(ctx)
		 }
	     res := new(event.ServerConfigEvent)
		 res.Operation = event.SET_CONFIG_RES
		 res.Cfg = ServerConfig
	    return res
   }
   return nil
}
