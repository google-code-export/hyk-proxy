/**
 * 
 */
package com.hyk.proxy.gae.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.hyk.compress.Compressor;
import com.hyk.compress.sevenzip.SevenZipCompressor;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;
import com.hyk.util.buffer.ByteArray;

/**
 * @author Administrator
 * 
 */
public class FatchServiceWrapper {
	private static final Logger log =
	      Logger.getLogger(XmppProxyServlet.class.getName());

	static Serializer serializer = new HykSerializer();
	static Compressor compressor = new SevenZipCompressor();

	private static HTTPHeader getHeader(HTTPResponse res, String name) {
		List<HTTPHeader> headers = new LinkedList<HTTPHeader>();
		for (HTTPHeader header : res.getHeaders()) {
			if (header.getName().equalsIgnoreCase(name)) {
				return header;
			}
		}
		return null;
	}
	
	private static HTTPHeader getHeader(HTTPRequest req, String name) {
		List<HTTPHeader> headers = new LinkedList<HTTPHeader>();
		for (HTTPHeader header : req.getHeaders()) {
			if (header.getName().equalsIgnoreCase(name)) {
				return header;
			}
		}
		return null;
	}
	
	private static List<HTTPHeader> getHeaders(HTTPResponse res, String name) {
		List<HTTPHeader> headers = new LinkedList<HTTPHeader>();
		for (HTTPHeader header : res.getHeaders()) {
			if (header.getName().equalsIgnoreCase(name)) {
				headers.add(header);
			}
		}
		return headers;
	}
	
	private static String getCookieHeadeValue(List<HTTPHeader> setcookie)
	{
		StringBuffer buffer = new StringBuffer();
		for (HTTPHeader httpHeader : setcookie) {
			buffer.append(httpHeader.getValue()).append(";");
		}
		return buffer.toString();
	}
	
	private static void cloneHeader(HTTPRequest oldReq, HTTPRequest newReq)
	{
//		List<HTTPHeader> headers = oldReq.getHeaders();
//		for (HTTPHeader httpHeader : headers) {
//			newReq.addHeader(httpHeader);
//		}
		newReq.addHeader(getHeader(oldReq, "Host"));
		newReq.addHeader(getHeader(oldReq, "User-Agent"));
		//newReq.addHeader(getHeader(oldReq, "User-Agent"));
	}

	public static ByteArray fetch(ByteArray req) throws IOException,
			InstantiationException {
		req = compressor.decompress(req);
		HttpRequestExchange fetchReqEx = serializer.deserialize(
				HttpRequestExchange.class, req);
		HTTPRequest fetchReq = fetchReqEx.toHTTPRequest();
		HTTPResponse fetchRes = URLFetchServiceFactory.getURLFetchService()
				.fetch(fetchReq);
		// HTTPHeader cookie = null;
		//HTTPHeader setcookie = getHeader(fetchRes, "set-cookie");
		

		HttpResponseExchange exchangeRes = new HttpResponseExchange(fetchRes);
		ByteArray rawRes = serializer.serialize(exchangeRes);
		return compressor.compress(rawRes);
	}
}
