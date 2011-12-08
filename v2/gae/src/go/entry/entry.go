package entry

import (
	"fmt"
	"http"
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
   InitEvents(new(DispatchEventHandler))
   ctx := appengine.NewContext(r)
   service.LoadServerConfig(ctx)
}

func AdminEntry(w http.ResponseWriter, r *http.Request) {
   
}
 
func IndexEntry(w http.ResponseWriter, r *http.Request) {
    ctx := appengine.NewContext(r)
    ctx.Infof("Requested URL: %v", r.URL)
	fmt.Fprint(w, "Hello, world!")
}

func XMPPEventDispatch(ctx appengine.Context, m *xmpp.Message){
}

func HTTPEventDispatch(w http.ResponseWriter, r *http.Request) {
    ctx := appengine.NewContext(r)
	buf := make([]byte, r.ContentLength)
	r.Body.Read(buf)
	success, ev, tags := event.ParseEventWithTags(buf)
	if success{
	   var attach [...]interface{}{tags, w, ctx}
	   ev.SetAttachment(&attach)
	   event.DiaptchEvent(ev)
	}
}
