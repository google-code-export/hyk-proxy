/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ProductReleaseDetail.java 
 *
 * @author qiying.wang [ May 17, 2010 | 2:13:47 PM ]
 *
 */
package com.hyk.proxy.framework.update;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 */
@XmlRootElement(name = "information")
public class ProductReleaseDetail
{
	public static class PluginReleaseDetail
	{
		@Override
        public String toString()
        {
	        return "PluginReleaseDetail [name=" + name + ", version=" + version
	                + ", url=" + url + ", desc=" + desc + "]";
        }
		@XmlAttribute
		public String name;
		@XmlAttribute
		public String version;
		@XmlElement
		public String url;
		@XmlElement
		public String desc;
	}

	public static class FrameworkReleaseDetail
	{
		@Override
        public String toString()
        {
	        return "FrameworkReleaseDetail [version=" + version + ", url="
	                + url + "]";
        }
		@XmlAttribute
		public String version;
		@XmlElement
		public String url;
	}

	@XmlElement(name = "framework")
	public FrameworkReleaseDetail framework;
	@XmlElementWrapper(name = "plugins")
	@XmlElements(@XmlElement(name = "plugin", type = PluginReleaseDetail.class))
	public List<PluginReleaseDetail> plugins;
}
