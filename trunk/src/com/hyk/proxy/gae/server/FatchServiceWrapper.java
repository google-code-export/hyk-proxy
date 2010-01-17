/**
 * 
 */
package com.hyk.proxy.gae.server;

import java.io.IOException;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.hyk.compress.Compressor;
import com.hyk.compress.sevenzip.SevenZipCompressor;
import com.hyk.proxy.gae.common.HttpRequestExchange;
import com.hyk.proxy.gae.common.HttpResponseExchange;
import com.hyk.serializer.HykSerializer;
import com.hyk.serializer.Serializer;

/**
 * @author Administrator
 *
 */
public class FatchServiceWrapper {

	static Serializer serializer = new HykSerializer();
	static Compressor compressor = new SevenZipCompressor();
	
	public static byte[] fetch(byte[] req) throws IOException, InstantiationException
	{
		req = compressor.decompress(req);
		HttpRequestExchange fetchReq = serializer.deserialize(
				HttpRequestExchange.class, req);
		HTTPResponse fetchRes = URLFetchServiceFactory.getURLFetchService()
		.fetch(fetchReq.toHTTPRequest());
		HttpResponseExchange exchangeRes = new HttpResponseExchange(
				fetchRes);
		byte[] rawRes = serializer.serialize(exchangeRes);
		return compressor.compress(rawRes);
	}
}
