/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ProductReleaseDetail.java 
 *
 * @author qiying.wang [ May 17, 2010 | 2:13:47 PM ]
 *
 */
package com.hyk.proxy.common.update;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 */
@XmlRootElement(name="information")
public class ProductReleaseDetail
{
	public static class Link
	{
		@XmlAttribute
		public String type;
		@XmlValue 
		public String link;
	}
	
	public static class ReleaseDetail
	{
		@XmlElement
		public String version;
		@XmlElements(@XmlElement(name = "link"))
		public List<Link> links;
	}
	
	@XmlElement(name="stable-release")
	public ReleaseDetail stableRelease;
	@XmlElement(name="latest-unstable-release")
	public ReleaseDetail latestUnstableRelease;
}
