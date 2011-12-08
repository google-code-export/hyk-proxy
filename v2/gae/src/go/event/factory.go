package event

type EventHandler interface {
	OnEvent(header *EventHeader, event Event)
}

type EventRegisterValue struct {
	creator func(Type uint32, Version uint32) Event
	handler *EventHandler
}

var RegistedEventTable map[uint64]EventRegisterValue

func getEventHandler(ev Event)(*EventHandler){
    var typeVer uint64
	typeVer = uint64(ev.GetType())<<32 + uint64(ev.GetVersion())
	handler, ok := RegistedEventTable[typeVer]
	if ok {
		return handler
	}
	return nil
}

func ParseEvent(buffer *bytes.Buffer) (bool, Event) {
	var header EventHeader
	if !header.Decode(buffer) {
		return false, nil
	}
	var typeVer uint64
	typeVer = uint64(header.Type)<<32 + uint64(header.Version)
	value, ok := RegistedEventTable[typeVer]
	if !ok {
		return false, nil
	}
	var event Event
	event = value.creator(header.Type, header.Version)
	return event.Decode(buffer), event
}

func ParseEventWithTags(buf []byte) (bool, Event, *EventHeaderTags) {
	var buffer = bytes.NewBuffer(buf)
	tags := new(EventHeaderTags)
	ok := tags.Decode(buffer)
	if !ok {
		return false, nil, nil
	}
	success, ev := ParseEvent(buffer)
	return success, ev, tags
}

func CreateEvent(Type uint32, Version uint32) Event {
	switch Type {
	case HTTP_REQUEST_EVENT_TYPE:
		return new(HTTPRequestEvent)
	case HTTP_RESPONSE_EVENT_TYPE:
		return new(HTTPResponseEvent)
	case RESERVED_SEGMENT_EVENT_TYPE:
		return new(SegmentEvent)
	case COMPRESS_EVENT_TYPE:
		return new(CompressEvent)
	case ENCRYPT_EVENT_TYPE:
		return new(EncryptEvent)
	case AUTH_REQUEST_EVENT_TYPE:
		return new(AuthRequestEvent)
	case USER_OPERATION_EVENT_TYPE:
		return nil
	case GROUP_OPERATION_EVENT_TYPE:
		return nil
	case USER_LIST_REQUEST_EVENT_TYPE:
		return nil
	case GROUOP_LIST_REQUEST_EVENT_TYPE:
		return nil
	case BLACKLIST_OPERATION_EVENT_TYPE:
		return nil
	case SERVER_CONFIG_EVENT_TYPE:
		return nil
	}
	return nil
}

func RegisterEventHandler(ev Event, handler *EventHandler) (ok bool, err string) {
	if nil == ev {
		return false, "Nil event!"
	}
	tmp1 := ev.GetType()
	tmp2 := ev.GetVersion()
	var key uint64 = uint64(tmp1)<<32 + uint64(tmp2)
	tmp, exist := RegistedEventTable[key]
	if exist {
		return false, "Duplicate event type"
	}
	tmp.creator = CreateEvent
	tmp.handler = handler
	//var value  EventRegisterValue
	//value.creator = CreateEvent
	//value.handler = handler
	RegistedEventTable[key] = tmp
	return true, ""
}

func DiaptchEvent(ev Event){
   handler := getEventHandler(ev)
   if nil != handler{
      var header EventHeader
      header.Type = ev.GetType()
      header.Version = ev.GetVersion()
      header.Hash = ev.GetHash()
      handler.OnEvent(&header, ev)
   }
}

func InitEvents(handler *EventHandler) {
    RegisterEventHandler(new (HTTP_REQUEST_EVENT_TYPE), handler)
    RegisterEventHandler(new (SegmentEvent),handler)
    RegisterEventHandler(new (EncryptEvent),handler)
    RegisterEventHandler(new (CompressEvent),handler)
    RegisterEventHandler(new (AuthRequestEvent),handler)
}
