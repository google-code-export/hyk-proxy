package entry

import (
	"appengine"
	"event"
	"service"
)

type DispatchEventHandler struct {

}

func (dispatcher *DispatchEventHandler) handleRecvEvent(header *EventHeader, ev Event) Event {
	var res Event
	var ctx appengine.Context
	ctx = ((ev.GetAttachment().([]interface{}))[2]).(appengine.Context)
	switch header.Type {
	case HTTP_REQUEST_EVENT_TYPE:
		res = service.Fetch(ev.(*HTTPRequestEvent))
	case RESERVED_SEGMENT_EVENT_TYPE:
		return new(SegmentEvent)
	case COMPRESS_EVENT_TYPE:
		compressEvent := ev.(*CompressEvent)
		dispatcher.handleRecvEvent(compressEvent.ev)
	case ENCRYPT_EVENT_TYPE:
		encryptEvent := ev.(*EncryptEvent)
		dispatcher.handleRecvEvent(encryptEvent.ev)
	case AUTH_REQUEST_EVENT_TYPE:
		res = service.Auth(ev.(*AuthRequestEvent))
	case USER_OPERATION_EVENT_TYPE:
		res = service.HandlerUserEvent(ev.(*AuthRequestEvent))
	case GROUP_OPERATION_EVENT_TYPE:
		res = service.HandlerUserEvent(ev.(*AuthRequestEvent))
	case USER_LIST_REQUEST_EVENT_TYPE:
		res = service.HandlerUserEvent(ev.(*AuthRequestEvent))
	case GROUOP_LIST_REQUEST_EVENT_TYPE:
		res = service.HandlerUserEvent(ev.(*AuthRequestEvent))
	case BLACKLIST_OPERATION_EVENT_TYPE:
		res = service.HandlerUserEvent(ev.(*AuthRequestEvent))
	case SERVER_CONFIG_EVENT_TYPE:
		res = service.HandlerConfigEvent(ev.(*AuthRequestEvent))
	}
	return res
}

func (dispatcher *DispatchEventHandler) OnEvent(header *EventHeader, event Event) {
	var res Event = handleRecvEvent(header, event)
	if nil != res {

	}
}
