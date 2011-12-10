package service

import (
	"appengine"
	"appengine/capability"
	"event"
	"rand"
	//"bytes"
	//"fmt"
)

const ANONYMOUSE_NAME = "anonymouse"
const SEED = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz~!@#$%^&*()-+<>"

func generateRandomString(n int) string {
	s := "";
	for i := 0; i < n; i++ {
		index := rand.Intn(len(SEED))
		s += SEED[index:index+1];
	}
	return s
}

func generateAuthToken(ctx appengine.Context) string {
	token := generateRandomString(10)
	if nil != GetUserWithToken(ctx, token) {
		return generateAuthToken(ctx)
	}
	return token
}

func Auth(ctx appengine.Context, ev *event.AuthRequestEvent)event.Event{
    user := GetUserWithName(ctx, ev.User)
    res := new (event.AuthResponseEvent)
    res.Appid = ev.Appid
    if nil != user{
       res.Token=user.AuthToken
    }else{
       res.Error="Invalid user/passwd."
    }
	return res
}

func HandlerUserEvent(ctx appengine.Context, ev *event.UserOperationEvent)event.Event{
    
	return nil
}

func HandlerGroupEvent(ctx appengine.Context, ev *event.GroupOperationEvent)event.Event{
    
	return nil
}

func CheckDefaultAccount(ctx appengine.Context) {
	if !capability.Enabled(ctx, "datastore_v3", "*") || !capability.Enabled(ctx, "memcache", "*") {
		return
	}
	CreateGroupIfNotExist(ctx, "root")
	CreateUserIfNotExist(ctx, "root", "root")
	CreateGroupIfNotExist(ctx, "public")
	CreateGroupIfNotExist(ctx, "anonymouse")
	CreateUserIfNotExist(ctx, "anonymouse", "anonymouse")
}

func CreateUserIfNotExist(ctx appengine.Context, email string, groupName string) {
	user := GetUserWithName(ctx, email)
	if nil == user {
		user = new(event.User)
		user.Email = email
		user.Group = groupName
		if email == ANONYMOUSE_NAME {
			user.Passwd = ANONYMOUSE_NAME
		} else {
			user.Passwd = generateRandomString(10)
		}
		user.AuthToken = generateAuthToken(ctx)
		SaveUser(ctx, user)
	}else{
	    if len(user.AuthToken) ==0{
	       user.AuthToken = generateAuthToken(ctx)
	       SaveUser(ctx, user)
	    }
	}
}

func CreateGroupIfNotExist(ctx appengine.Context, name string) {
	group := GetGroup(ctx, name)
	if nil == group {
		group = new(event.Group)
		group.Name = name
		SaveGroup(ctx, group)
	}
}


