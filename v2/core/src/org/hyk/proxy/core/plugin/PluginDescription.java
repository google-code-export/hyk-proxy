/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: PluginDescription.java 
 *
 * @author yinqiwen [ 2010-6-15 | 03:19:20 PM ]
 *
 */
package org.hyk.proxy.core.plugin;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name = "plugin")
public class PluginDescription
{
	@XmlAttribute
	public String name;
	
	@XmlAttribute
	public String version;
	
	@XmlElement
	public String description;
	
	@XmlElement
	public String entryClass;
	
	@XmlElements(@XmlElement(name = "depend"))
	public List<String> depends;
	
	
	
}
