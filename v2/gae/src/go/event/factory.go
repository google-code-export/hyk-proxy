package event

type EventHandler interface {
	OnEvent(header *EventHeader, event Event)
}

type EventRegisterValue struct {
	creator func(Type uint32, Version uint32) Event
	handler *EventHandler
}

var RegistedEventTable map[uint64]EventRegisterValue

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
		{
		}
	case RESERVED_SEGMENT_EVENT_TYPE:
		{
		}
	case COMPRESS_EVENT_TYPE:
		{
		}
	case ENCRYPT_EVENT_TYPE:
		{
		}
	case AUTH_REQUEST_EVENT_TYPE:
		{
		}
	case USER_OPERATION_EVENT_TYPE:
		{
		}
	case GROUP_OPERATION_EVENT_TYPE:
		{
		}
	case USER_LIST_REQUEST_EVENT_TYPE:
		{
		}
	case GROUOP_LIST_REQUEST_EVENT_TYPE:
		{
		}
	case BLACKLIST_OPERATION_EVENT_TYPE:
		{
		}
	case SERVER_CONFIG_EVENT_TYPE:
		{
		}
	}
	return nil
}

func RegisterEventHandler(ev Event, handler *EventHandler) (ok bool, err string) {
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