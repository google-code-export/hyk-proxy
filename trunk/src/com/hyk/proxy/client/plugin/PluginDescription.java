/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: PluginDescription.java 
 *
 * @author yinqiwen [ 2010-6-15 | 03:19:20 PM ]
 *
 */
package com.hyk.proxy.client.plugin;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name = "plugin")
public class PluginDescription
{
	@XmlElement
	String entryClass;
	
	@XmlElements(@XmlElement(name = "depend"))
	List<String> depends;
}
