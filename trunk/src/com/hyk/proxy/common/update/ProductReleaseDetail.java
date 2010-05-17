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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name="information")
public class ProductReleaseDetail
{
	public static class ReleaseDetail
	{
		@XmlElement
		public String version;
		@XmlElement
		public String link;
	}
	
	@XmlElement(name="stable-release")
	public ReleaseDetail stableRelease;
	@XmlElement(name="latest-unstable-release")
	public ReleaseDetail latestUnstableRelease;
}
