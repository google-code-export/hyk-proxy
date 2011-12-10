package entry

import (
	"fmt"
	"http"
	"bytes"
	"encoding/base64"
	"strconv"
	"appengine"
	"appengine/user"
	"appengine/xmpp"
	"event"
	"service"
	"handler"
)

const Version = "0.10.1"

var serverInited bool = false

func init() {
	event.InitEvents(new(handler.DispatchEventHandler))
	http.HandleFunc("/", IndexEntry)
	http.HandleFunc("/admin", AdminEntry)
	//http.HandleFunc("/signout", SignoutEntry)
	http.HandleFunc("/invoke", HTTPEventDispatch)
	//warmup request is no available in GO runtime now
	//http.HandleFunc("/_ah/warmup", InitGAEServer)
	xmpp.RegisterChatHandler(XMPPEventDispatch)
	//http.HandleFunc("/_ah/xmpp/message/chat/", XMPPEventDispatch)
}

func initGAEProxyServer(ctx appengine.Context) {
	if !serverInited {
		service.LoadServerConfig(ctx)
		service.CheckDefaultAccount(ctx)
		ctx.Infof("InitGAEServer Invoked!")
		serverInited = true
	}
}

//func InitGAEServer(w http.ResponseWriter, r *http.Request) {
//	ctx := appengine.NewContext(r)
//
//	w.WriteHeader(http.StatusOK)
//	
//}

const adminFrom = `
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>hyk-proxy-go-gae V%s admin</title>
  </head>
  <body>
    <table width="800" border="0" align="center">
            <tr><td align="center">
                <b><h1>root password:%s</h1></b>
            </td></tr>
             <tr><td align="center">
                <a href="%s">sign out</a>
            </td></tr>
    </table>
  </body>
</html>
`

const signoutFrom = `
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>hyk-proxy-go-gae %s admin</title>
  </head>
  
   <body>
    <table width="800" border="0" align="center">
            <tr><td align="center">
                <p>Hello, %s! You are not the admin of this application, please 
<a href="%s">sign out</a> first, then login again.</p>
            </td></tr>
    </table>
    
  </body>
</html>
`

func AdminEntry(w http.ResponseWriter, r *http.Request) {
	c := appengine.NewContext(r)
	u := user.Current(c)
	if u == nil {
		url, err := user.LoginURL(c, r.URL.String())
		if err != nil {
			http.Error(w, err.String(), http.StatusInternalServerError)
			return
		}
		w.Header().Set("Location", url)
		w.WriteHeader(http.StatusFound)
		return
	}
	if !user.IsAdmin(c){
	    url,_ := user.LogoutURL(c, "/admin")
	   fmt.Fprintf(w, signoutFrom, Version,u.String(), url)
	   return
	}
	url,_ := user.LogoutURL(c, "/")
	root := service.GetUserWithName(c, "root")
	fmt.Fprintf(w, adminFrom, Version, root.Passwd, url)
}

const indexForm = `
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>hyk-proxy-gae %s</title>
  </head>

  <body>
    <table width="800" border="0" align="center">
            <tr><td align="center">
                <b><h1>hyk-proxy-go-gae %s server is running!</h1></b>
            </td></tr>
            <tr><td align="center">
                <a href="/admin">admin</a>
            </td></tr>
    </table>
  </body>
</html>
`

func IndexEntry(w http.ResponseWriter, r *http.Request) {
	//ctx := appengine.NewContext(r)
	fmt.Fprintf(w, indexForm, Version, Version)
}



type HTTPEventSendService struct {
	writer http.ResponseWriter
}

func (serv *HTTPEventSendService) GetMaxDataPackageSize() int {
	return -1
}
func (serv *HTTPEventSendService) Send(buf *bytes.Buffer) {
	headers := serv.writer.Header()
	headers.Add("Content-Type", "application/octet-stream")
	headers.Add("Content-Length", strconv.Itoa(buf.Len()))
	serv.writer.WriteHeader(http.StatusOK)
	serv.writer.Write(buf.Bytes())
}

type XMPPEventSendService struct {
	jid  string
	from string
	ctx  appengine.Context
}

func (serv *XMPPEventSendService) GetMaxDataPackageSize() int {
	return int(service.ServerConfig.MaxXMPPDataPackageSize)
}
func (serv *XMPPEventSendService) Send(buf *bytes.Buffer) {
	body := base64.StdEncoding.EncodeToString(buf.Bytes())
	var msg xmpp.Message
	msg.Body = body
	msg.Sender = serv.jid
	msg.To = []string{serv.from}
	msg.Type = "chat"
	msg.Send(serv.ctx)
}

func XMPPEventDispatch(ctx appengine.Context, m *xmpp.Message) {
	initGAEProxyServer(ctx)
	src, err := base64.StdEncoding.DecodeString(m.Body)
	if nil != err {
		ctx.Errorf("Failed to decode base64 XMPP.")
		return
	}
	success, ev, tags, cause := event.ParseEventWithTags(src)
	if success {
		serv := new(XMPPEventSendService)
		serv.jid = m.To[0]
		serv.from = m.Sender
		serv.ctx = ctx
		attach := make([]interface{}, 3)
		attach[0] = tags
		attach[1] = ctx
		attach[2] = serv
		ev.SetAttachement(attach)
		event.DiaptchEvent(ev)
		return
	}
	ctx.Errorf("Failed to parse XMPP event:" + cause)
}

func HTTPEventDispatch(w http.ResponseWriter, r *http.Request) {
	ctx := appengine.NewContext(r)
	initGAEProxyServer(ctx)
	buf := make([]byte, r.ContentLength)
	r.Body.Read(buf)
	serv := new(HTTPEventSendService)
	serv.writer = w
	success, ev, tags, cause := event.ParseEventWithTags(buf)
	if success {
		attach := make([]interface{}, 3)
		attach[0] = tags
		attach[1] = ctx
		attach[2] = serv
		ev.SetAttachement(attach)
		event.DiaptchEvent(ev)
		return
	}
	ctx.Errorf("Failed to parse HTTP event:" + cause)
}
