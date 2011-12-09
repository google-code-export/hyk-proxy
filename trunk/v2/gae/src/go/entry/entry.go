package entry

import (
	"fmt"
	"http"
	"bytes"
	"strconv"
	"appengine"
	"appengine/xmpp"
	"event"
	"service"
	"handler"
)

func init() {
	http.HandleFunc("/", IndexEntry)
	http.HandleFunc("/admin", AdminEntry)
	http.HandleFunc("/invoke", HTTPEventDispatch)
	http.HandleFunc("/_ah/warmup", InitGAEServer)
	xmpp.RegisterChatHandler(XMPPEventDispatch)
	//http.HandleFunc("/_ah/xmpp/message/chat/", XMPPEventDispatch)
}

func InitGAEServer(w http.ResponseWriter, r *http.Request) {
   event.InitEvents(new(handler.DispatchEventHandler))
   ctx := appengine.NewContext(r)
   service.LoadServerConfig(ctx)
   service.CheckDefaultAccount(ctx)
}

func AdminEntry(w http.ResponseWriter, r *http.Request) {
   
}
 
func IndexEntry(w http.ResponseWriter, r *http.Request) {
    ctx := appengine.NewContext(r)
    ctx.Infof("Requested URL: %v", r.URL)
	fmt.Fprint(w, "Hello, world!")
}

type HTTPEventSendService struct{
    writer http.ResponseWriter
}

func (serv *HTTPEventSendService)GetMaxDataPackageSize()int{
   return -1
}
func (serv *HTTPEventSendService)Send(buf *bytes.Buffer){
   headers := serv.writer.Header()
   headers.Add("Content-Type", "application/octet-stream")
   headers.Add("Content-Length", strconv.Itoa(buf.Len()))
   serv.writer.WriteHeader(http.StatusOK)
   serv.writer.Write(buf.Bytes())
}

func XMPPEventDispatch(ctx appengine.Context, m *xmpp.Message){
}

func HTTPEventDispatch(w http.ResponseWriter, r *http.Request) {
    ctx := appengine.NewContext(r)
	buf := make([]byte, r.ContentLength)
	r.Body.Read(buf)
	serv := new(HTTPEventSendService)
	serv.writer = w;
	success, ev, tags := event.ParseEventWithTags(buf)
	if success{
	   attach := make([]interface{}, 4)
	   attach[0] = tags
	   attach[1] = ctx
	   attach[2]= serv
	   ev.SetAttachement(attach)
	   event.DiaptchEvent(ev)
	}
}



