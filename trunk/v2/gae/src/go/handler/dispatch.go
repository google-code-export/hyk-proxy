package handler

import (
	"appengine"
	"event"
	"service"
)

type DispatchEventHandler struct {

}

func (dispatcher *DispatchEventHandler) handleRecvEvent(ctx appengine.Context, header *event.EventHeader, ev event.Event) event.Event {
	var res event.Event
	switch header.Type {
	case event.HTTP_REQUEST_EVENT_TYPE:
		res = service.Fetch(ctx, ev.(*event.HTTPRequestEvent))
	case event.RESERVED_SEGMENT_EVENT_TYPE:
		return new(event.SegmentEvent)
	case event.COMPRESS_EVENT_TYPE:
		compressEvent := ev.(*event.CompressEvent)
		var tmp = event.EventHeader{compressEvent.GetType(), compressEvent.GetVersion(), header.Hash}
		dispatcher.handleRecvEvent(ctx, &tmp, compressEvent.Ev)
	case event.ENCRYPT_EVENT_TYPE:
		encryptEvent := ev.(*event.EncryptEvent)
		var tmp = event.EventHeader{encryptEvent.GetType(), encryptEvent.GetVersion(), header.Hash}
		dispatcher.handleRecvEvent(ctx, &tmp, encryptEvent.Ev)
	case event.AUTH_REQUEST_EVENT_TYPE:
		//res = service.Auth(ev.(*event.AuthRequestEvent))
	case event.USER_OPERATION_EVENT_TYPE:
		//res = service.HandlerUserEvent(ev.(*event.AuthRequestEvent))
	case event.GROUP_OPERATION_EVENT_TYPE:
		//res = service.HandlerUserEvent(ev.(*event.AuthRequestEvent))
	case event.USER_LIST_REQUEST_EVENT_TYPE:
		//res = service.HandlerUserEvent(ev.(*event.AuthRequestEvent))
	case event.GROUOP_LIST_REQUEST_EVENT_TYPE:
		//res = service.HandlerUserEvent(ev.(*event.AuthRequestEvent))
	case event.BLACKLIST_OPERATION_EVENT_TYPE:
		//res = service.HandlerUserEvent(ev.(*event.AuthRequestEvent))
	case event.SERVER_CONFIG_EVENT_TYPE:
		//res = service.HandlerConfigEvent(ev.(*event.AuthRequestEvent))
	}
	return res
}

func (dispatcher *DispatchEventHandler) OnEvent(header *event.EventHeader, ev event.Event) {
	var ctx appengine.Context
	ctx = ((ev.GetAttachement().([]interface{}))[1]).(appengine.Context)
	sendservice = ((ev.GetAttachement().([]interface{}))[2]).(service.EventSendService)
	var res event.Event = dispatcher.handleRecvEvent(ctx, header, ev)
	if nil != res {
       res.SetHash(ev.GetHash())
       
	}else{
	   
	}
}
