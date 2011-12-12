package service

import (
	"appengine"
	"appengine/urlfetch"
	"event"
	"strconv"
	"http"
	"fmt"
)

func buildHTTPRequest(ev *event.HTTPRequestEvent) *http.Request {
	req, err := http.NewRequest(ev.Method, ev.Url, &(ev.Content))
	if err != nil {
		return nil
	}
	var slen int = ev.Headers.Len()
	for i := 0; i < slen; i++ {
		header, ok := ev.Headers.At(i).(*event.NameValuePair)
		if ok {
			req.Header.Add(header.Name, header.Value)
		}
	}
	//if ev.Content.Len() > 0{
	//  req.Body.Read(ev.Content.Bytes())
	//}
	return req
}

func buildHTTPResponseEvent(res *http.Response) *event.HTTPResponseEvent {
	ev := new(event.HTTPResponseEvent)
	ev.Status = uint32(res.StatusCode)
	for key, values := range res.Header {
		for _, value := range values {
			ev.AddHeader(key, value)
		}
	}
	b := make([]byte, res.ContentLength)
	if res.ContentLength > 0 {
		res.Body.Read(b)
		ev.Content.Write(b)
	}
	return ev
}

func fillErrorResponse(ev *event.HTTPResponseEvent, cause string) {
	str := "You are not allowed to visit this site via proxy because %s."
	content := fmt.Sprintf(str, cause)
	ev.SetHeader("Content-Type", "text/plain")
	ev.SetHeader("Content-Length", strconv.Itoa(len(content)))
	ev.Content.WriteString(content)
}

func Fetch(context appengine.Context, ev *event.HTTPRequestEvent) event.Event {
	req := buildHTTPRequest(ev)
	errorResponse := new(event.HTTPResponseEvent)
	if req == nil {
		errorResponse.Status = 400
		fillErrorResponse(errorResponse, "Invalid fetch url:"+ev.Url)
		return errorResponse
	}
	t := &urlfetch.Transport{context, 0, true}
	retryCount := ServerConfig.RetryFetchCount
	for retryCount > 0 {
		resp, err := t.RoundTrip(req)
		if err == nil {
			return buildHTTPResponseEvent(resp)
		}
		context.Errorf("Failed to fetch URL[%s] for reason:%s", ev.Url, err.String())
		retryCount--
		if req.Header.Get("Range") == "" {
			rangeLimit := ServerConfig.RangeFetchLimit
			req.Header.Set("Range", strconv.Itoa64(int64(rangeLimit-1)))
		}
	}
	errorResponse.Status = 408
	fillErrorResponse(errorResponse, "Fetch timeout for url:"+ev.Url)
	return errorResponse

}
