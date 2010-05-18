/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: UpdateCheck.java 
 *
 * @author yinqiwen [ 2010-5-16 | ÏÂÎç07:06:21 ]
 *
 */
package com.hyk.proxy.common.update;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.http.message.HttpRequestExchange;
import com.hyk.proxy.common.http.message.HttpResponseExchange;
import com.hyk.proxy.common.rpc.service.FetchService;

/**
 *
 */
public class UpdateCheck 
{
	
	protected Logger	logger	= LoggerFactory.getLogger(getClass());
	private FetchService fetchService;
	
	public UpdateCheck(FetchService fetchService)
	{
		this.fetchService = fetchService;
	}

	private HttpRequestExchange createVersionQueryHttpRequest()
	{
		HttpRequestExchange req = new HttpRequestExchange();
		req.method = "GET";
		req.url = Constants.LATEST_VERSION;
		return req;
	}

	public UpdateCheckResults checkForUpdates()
	{
		HttpResponseExchange res = fetchService.fetch(createVersionQueryHttpRequest());
		if(res.responseCode == 200)
		{
			String value = new String(res.getBody());
			try
			{
				JAXBContext context = JAXBContext.newInstance(ProductReleaseDetail.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));
				ProductReleaseDetail latestVersion = (ProductReleaseDetail)unmarshaller.unmarshal(is);
				return new UpdateCheckResults(latestVersion);
			}
			catch(Exception e)
			{
				logger.error("Failed to retrieve latest product version.", e);
			}
			
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(ProductReleaseDetail.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			InputStream is = new FileInputStream("update/version.xml");
			ProductReleaseDetail latestVersion = (ProductReleaseDetail)unmarshaller.unmarshal(is);
			System.out.println(latestVersion.stableRelease.version);
			System.out.println(latestVersion.stableRelease.links.get(0).type);
			System.out.println(latestVersion.stableRelease.links.get(0).link);
			System.out.println(latestVersion.stableRelease.links.get(1).type);
			System.out.println(latestVersion.stableRelease.links.get(1).link);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
