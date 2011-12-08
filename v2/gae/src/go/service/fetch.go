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
	req, err := http.NewRequest(ev.Method, ev.Url, ev.Content)
	if err != nil {
		return nil
	}
	var slen int = ev.Headers.Len()
	for i := 0; i < slen; i++ {
		header, ok := ev.Headers.At(i).([]string)
		if ok {
			req.Header.Add(header[0], header[1])
		}
	}
	return req
}

func buildHTTPResponseEvent(res *http.Response) *event.HTTPResponseEvent {
	ev := new(event.HTTPResponseEvent)
	ev.Status = res.StatusCode
	for key, value := range res.Header {
		ev.Headers.Push([]string{key, value})
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
	ev.SetHeader("Content-Length", Itoa(len(content)))
	ev.Content.WriteString(content)
}

func Fetch(context appengine.Context, ev *event.HTTPRequestEvent) Event {
	req := buildHTTPRequest(ev)
	errorResponse := new(HTTPResponseEvent)
	if req == nil {
		errorResponse.Status = 400
		fillErrorResponse(errorResponse, "Invalid fetch url:"+ev.Url)
		return errorResponse
	}
	t := &urlfetch.Transport{context, 10.0, true}
	retryCount := service.ServerConfig.RetryFetchCount
	for retryCount > 0 {
		resp, err := t.RoundTrip(req)
		if err == nil {
			return buildHTTPResponseEvent(resp)
		}
		retryCount--
		if req.Header.Get("Range") == "" {
		   rangeLimit := service.ServerConfig.RangeFetchLimit
		   req.Header.Set("Range", Itoa(rangeLimit - 1))
		}
	}
	errorResponse.Status = 408
	fillErrorResponse(errorResponse, "Fetch timeout for url:"+ev.Url)
	return errorResponse

}
