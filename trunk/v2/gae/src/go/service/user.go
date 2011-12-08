package service

import (
	"appengine"
	"appengine/datastore"
	"appengine/memcache"
	"appengine/capability"
	"event"
	"bytes"
	"codec"
	"strings"
	"strconv"
)

var UserTable map[string]*event.User
var GroupTable map[string]*event.Group

func User2PropertyList(user *event.User) datastore.PropertyList {
	var ret = make([]datastore.PropertyList, 0, 6)
	ret = append(ret, datastore.Property{
		Name:  "Name",
		Value: user.Email,
	})
	ret = append(ret, datastore.Property{
		Name:  "Passwd",
		Value: user.Passwd,
	})
	ret = append(ret, datastore.Property{
		Name:  "Group",
		Value: user.Group,
	})
	ret = append(ret, datastore.Property{
		Name:  "AuthToken",
		Value: user.AuthToken,
	})
	var tmp string = ""
	for key, _ := range user.BlackList {
		tmp += key
		tmp += ";"
	}
	ret = append(ret, datastore.Property{
		Name:  "BlackList",
		Value: tmp,
	})
	return ret
}

func PropertyList2User(props datastore.PropertyList) *event.User {

	return ret
}

func Group2PropertyList(group *event.Group) datastore.PropertyList {
	var ret = make([]datastore.PropertyList, 0, 2)
	ret = append(ret, datastore.Property{
		Name:  "Name",
		Value: group.Email,
	})
	var tmp string = ""
	for key, _ := range group.BlackList {
		tmp += key
		tmp += ";"
	}
	ret = append(ret, datastore.Property{
		Name:  "BlackList",
		Value: tmp,
	})
	return ret
}

func PropertyList2Group(props datastore.PropertyList) *event.Group {

	return ret
}

const USER_CACHE_KEY_PREFIX = "ProxyUser:"
const GROUP_CACHE_KEY_PREFIX = "ProxyGroup:"

func SaveUser(ctx appengine.Context, user *event.User) {
	props = User2PropertyList(user)
	key := datastore.NewKey(ctx, "ProxyUser", user.Email, 0, nil)
	_, err := datastore.Put(ctx, key, props)
	if err != nil {
		//ctx.LogE()
		return
	}
	var buf bytes.Buffer
	user.Encode(&buf)
	memitem1 := &memcache.Item{
		Key:   USER_CACHE_KEY_PREFIX + user.Email,
		Value: buf.Bytes(),
	}
	memitem2 := &memcache.Item{
		Key:   USER_CACHE_KEY_PREFIX + user.AuthToken,
		Value: buf.Bytes(),
	}
	// Add the item to the memcache, if the key does not already exist
	memcache.Set(ctx, memitem1)
	memcache.Set(ctx, memitem2)
	UserTable[USER_CACHE_KEY_PREFIX+user.AuthToken] = user
	UserTable[USER_CACHE_KEY_PREFIX+user.Email] = user
}

func GetUserFromCache(ctx appengine.Context, name string) *event.User {
	user, exist = UserTable[USER_CACHE_KEY_PREFIX+name]
	if exist {
		return user
	}
	if item, err := memcache.Get(ctx, USER_CACHE_KEY_PREFIX+name); err == nil {
		buf := bytes.NewBuffer(item.Value)
		user = new(event.User)
		if user.Decode(buf) {
			UserTable[USER_CACHE_KEY_PREFIX+name] = user
			return user
		}
	}
	return nil
}

func GetUserWithName(ctx appengine.Context, name string) *event.User {
	user := GetUserFromCache(name)
	if nil != user {
		return user
	}
	var item datastore.PropertyList
	key := datastore.NewKey(ctx, "ProxyUser", name, 0, nil)
	if err := datastore.Get(ctx, key, item); err != nil && err != datastore.ErrNoSuchEntity {
		return
	}
	user = PropertyList2User(item)
	var buf bytes.Buffer
	user.Encode(&buf)
	memitem := &memcache.Item{
		Key:   USER_CACHE_KEY_PREFIX + user.Email,
		Value: buf.Bytes(),
	}
	memitem2 := &memcache.Item{
		Key:   USER_CACHE_KEY_PREFIX + user.AuthToken,
		Value: buf.Bytes(),
	}
	memcache.Set(ctx, memitem)
	memcache.Set(ctx, memitem2)
}

func GetUserWithToken(ctx appengine.Context, token string) *event.User {
	user := GetUserFromCache(name)
	if nil != user {
		return user
	}
	q := datastore.NewQuery("ProxyUser").Filter("AuthToken =", token)
	props := make(datastore.PropertyList, 0, 10)
	if _, err := q.GetAll(ctx, &props); err != nil {
		//http.Error(w, err.String(), http.StatusInternalServerError)
		return nil
	}
	for x := range props {
		user = PropertyList2User(x)
		if nil != user {
			return user
		}
	}
	return nil
}

func GetAllUsers(ctx appengine.Context) []*event.User {
	q := datastore.NewQuery("ProxyUser")
	props := make(datastore.PropertyList, 0, 10)
	if _, err := q.GetAll(ctx, &props); err != nil {
		//http.Error(w, err.String(), http.StatusInternalServerError)
		return nil
	}
	users := make(*event.User, 0, len(props))
	for x := range props {
		user = PropertyList2User(x)
		users = append(users, user)
	}
	return users
}

func SaveGroup(ctx appengine.Context, grp *event.Group) {
	props = Group2PropertyList(grp)
	key := datastore.NewKey(ctx, "ProxyGroup", grp.Name, 0, nil)
	_, err := datastore.Put(ctx, key, props)
	if err != nil {
		//ctx.LogE()
		return
	}
	var buf bytes.Buffer
	grp.Encode(&buf)
	memitem1 := &memcache.Item{
		Key:   GROUP_CACHE_KEY_PREFIX + group.Name,
		Value: buf.Bytes(),
	}
	// Add the item to the memcache, if the key does not already exist
	memcache.Set(ctx, memitem1)
	GroupTable[GROUP_CACHE_KEY_PREFIX+group.Name] = group
}

func GetGroup(ctx appengine.Context, name string) *event.Group {
	group, exist := GroupTable[GROUP_CACHE_KEY_PREFIX+name]
	if exist {
		return group
	}
	if item, err := memcache.Get(ctx, GROUP_CACHE_KEY_PREFIX+name); err == nil {
		buf := bytes.NewBuffer(item.Value)
		group = new(event.Group)
		if group.Decode(buf) {
			GroupTable[GROUP_CACHE_KEY_PREFIX+name] = group
			return group
		}
	}
	var item datastore.PropertyList
	key := datastore.NewKey(ctx, "ProxyGroup", name, 0, nil)
	if err := datastore.Get(ctx, key, item); err != nil && err != datastore.ErrNoSuchEntity {
		return
	}
	group = PropertyList2Group(item)
	var buf bytes.Buffer
	group.Encode(&buf)
	memitem := &memcache.Item{
		Key:   GROUP_CACHE_KEY_PREFIX + group.Name,
		Value: buf.Bytes(),
	}
	memcache.Set(ctx, memitem)
}

func GetAllGroups(ctx appengine.Context) []*event.Group {
	q := datastore.NewQuery("ProxyGroup")
	props := make(datastore.PropertyList, 0, 10)
	if _, err := q.GetAll(ctx, &props); err != nil {
		//http.Error(w, err.String(), http.StatusInternalServerError)
		return nil
	}
	groups := make(*event.Group, 0, len(props))
	for x := range props {
		group = PropertyList2Group(x)
		groups = append(groups, group)
	}
	return groups
}

func DeleteUser(ctx appengine.Context, user *event.User) {
	key := datastore.NewKey(ctx, "ProxyUser", user.Email, 0, nil)
	datastore.delete(ctx, key)
	key1 := USER_CACHE_KEY_PREFIX + User.Email
	key2 := USER_CACHE_KEY_PREFIX + User.AuthToken
	UserTable[key1] = nil, false
	UserTable[key2] = nil, false
	memcache.Delete(key1)
	memcache.Delete(key2)
}

func DeleteGroup(ctx appengine.Context, group *event.Group) {
	key := datastore.NewKey(ctx, "ProxyGroup", group.Name, 0, nil)
	datastore.delete(ctx, key)
	key1 := GROUP_CACHE_KEY_PREFIX + group.Name
	GroupTable[key1] = nil, false
	memcache.Delete(key1)
}

func IsRootUser(ctx appengine.Context, token string) bool {
	user := GetUserWithToken(ctx, token)
	if nil != user && user.Name == "Root" {
		return true
	}
	return false
}

func UserAuthServiceAvailable(ctx appengine.Context) bool {
	if !capability.Enabled(ctx, "datastore_v3", "*") {
		return false
	}
	if !capability.Enabled(ctx, "memcache", "*") {
		return false
	}
	return true
}
