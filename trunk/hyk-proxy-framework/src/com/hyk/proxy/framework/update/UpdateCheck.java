/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: UpdateCheck.java 
 *
 * @author yinqiwen [ 2010-5-16 | 07:06:21 PM ]
 *
 */
package com.hyk.proxy.framework.update;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.common.Constants;
import com.hyk.proxy.framework.config.Config;
import com.hyk.proxy.framework.util.CommonUtil;
import com.hyk.proxy.framework.util.SimpleSocketAddress;

/**
 *
 */
public class UpdateCheck
{

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public UpdateCheckResults checkForUpdates()
	{
		try
		{
			return new UpdateCheckResults(getProductReleaseDetail(),
			        getProductUpdateDetail());
		}
		catch (Exception e)
		{
			logger.error("Failed to check update info.", e);
			return null;
		}

	}

	public ProductReleaseDetail getProductReleaseDetail()
	        throws Exception
	{
		URLConnection releaseConn = CommonUtil.openRemoteDescriptionFile(Constants.LATEST_VERSION);
		if(null == releaseConn)
		{
			return null;
		}
		JAXBContext releaseContext = JAXBContext
		        .newInstance(ProductReleaseDetail.class);
		Unmarshaller releaseUnmarshaller = releaseContext.createUnmarshaller();
		return (ProductReleaseDetail) releaseUnmarshaller.unmarshal(releaseConn
		        .getInputStream());

	}

	public ProductUpdateDetail getProductUpdateDetail()
	        throws Exception
	{

		URLConnection updateConn = CommonUtil.openRemoteDescriptionFile(Constants.UPDATES);
		if (null == updateConn)
		{
			return null;
		}

		JAXBContext updateContext = JAXBContext
		        .newInstance(ProductUpdateDetail.class);
		Unmarshaller updateUnmarshaller = updateContext.createUnmarshaller();
		return (ProductUpdateDetail) updateUnmarshaller.unmarshal(updateConn
		        .getInputStream());

	}

	public UpdateCheckResults checkForUpdates(InputStream releaseInputStream,
	        InputStream updateInputStream) throws JAXBException
	{
		JAXBContext releaseContext = JAXBContext
		        .newInstance(ProductReleaseDetail.class);
		Unmarshaller releaseUnmarshaller = releaseContext.createUnmarshaller();
		ProductReleaseDetail latestVersion = (ProductReleaseDetail) releaseUnmarshaller
		        .unmarshal(releaseInputStream);

		JAXBContext updateContext = JAXBContext
		        .newInstance(ProductUpdateDetail.class);
		Unmarshaller updateUnmarshaller = updateContext.createUnmarshaller();
		ProductUpdateDetail updateDetail = (ProductUpdateDetail) updateUnmarshaller
		        .unmarshal(updateInputStream);

		return new UpdateCheckResults(latestVersion, updateDetail);
	}

}
