/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: UpdateCheck.java 
 *
 * @author yinqiwen [ 2010-5-16 | ÏÂÎç07:06:21 ]
 *
 */
package com.hyk.proxy.client.update;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.hyk.proxy.client.config.Config;
import com.hyk.proxy.common.http.message.HttpResponseExchange;
import com.hyk.proxy.common.rpc.service.FetchService;
import com.hyk.proxy.common.update.ReleaseVersion;
import com.hyk.proxy.common.update.UpdateUtil;

/**
 *
 */
public class UpdateCheck implements Runnable
{

	private FetchService fetchService;
	@Override
	public void run()
	{
		HttpResponseExchange res = fetchService.fetch(UpdateUtil.createVersionQueryHttpRequest());
		if(res.responseCode == 200)
		{
			String value = new String(res.getBody());
			try
			{
				JAXBContext context = JAXBContext.newInstance(ReleaseVersion.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));
				ReleaseVersion latestVersion = (ReleaseVersion)unmarshaller.unmarshal(is);
				
			}
			catch(Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
