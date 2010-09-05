/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ProductUpdateDetail.java 
 *
 * @author yinqiwen [ 2010-8-21 | 02:21:40 PM]
 *
 */
package com.hyk.proxy.framework.update;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.hyk.proxy.framework.launch.Upgrade.UpgradeAction;


/**
 *
 */
@XmlRootElement(name = "information")
public class ProductUpdateDetail
{

	public static class UpgradeFileset
	{
		@XmlAttribute
		public UpgradeAction action = UpgradeAction.REPLACE;
		@XmlElements(@XmlElement(name = "file", type = String.class))
		public List<String> fileset;
	}
	
	public static class CommonUpgradeDetail
	{
		@Override
        public String toString()
        {
	        return "CommonUpgradeDetail [to=" + to + ", from=" + from
	                + ", fileset=" + filesets + "]";
        }
		@XmlAttribute
		public String to;
		@XmlAttribute
		public String from;
		//@XmlElementWrapper(name = "fileset")
		@XmlElements(@XmlElement(name = "fileset", type = UpgradeFileset.class))
		public List<UpgradeFileset> filesets;
	}

	public static class FrameworkUpdateDetail
	{
		@Override
        public String toString()
        {
	        return "FrameworkUpdateDetail [details=" + detail + "]";
        }

		@XmlElement(name = "upgrade")
		public CommonUpgradeDetail detail;
	}

	public static class PluginUpdateDetail
	{
		@Override
        public String toString()
        {
	        return "PluginUpdateDetail [name=" + name + ", details=" + detail
	                + "]";
        }
		@XmlAttribute
		public String name;
		@XmlElement(name = "upgrade")
		public CommonUpgradeDetail detail;
	}

	@XmlElement(name = "framework")
	public FrameworkUpdateDetail framework;

	@XmlElementWrapper(name = "plugins")
	@XmlElements(@XmlElement(name = "plugin", type = PluginUpdateDetail.class))
	public List<PluginUpdateDetail> plugins;
}
