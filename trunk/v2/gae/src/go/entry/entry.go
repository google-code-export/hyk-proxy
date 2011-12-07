package entry

import (
	"fmt"
	"http"
	"event"
	"handler"
)

func init() {
	http.HandleFunc("/", handler)
	http.HandleFunc("/invoke", HTTPEventDispatch)
	//http.HandleFunc("/_ah/xmpp/message/chat/", XMPPEventDispatch)
}

func handler(w http.ResponseWriter, r *http.Request) {
	fmt.Fprint(w, "Hello, world!")
}

func HTTPEventDispatch(w http.ResponseWriter, r *http.Request) {
	buf := make([]byte, r.ContentLength)
	r.Body.Read(buf)
	success, event, tags := event.ParseEventWithTags(buf)
}
