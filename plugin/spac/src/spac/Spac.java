/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Spac.java 
 *
 * @author yinqiwen [ 2010-6-14 | 08:57:05 PM ]
 *
 */
package spac;

import org.tykedog.csl.interpreter.CSL;

import spac.event.SpacProxyEventServiceFactory;
import spac.event.forward.DirectProxyEventServiceFactory;
import spac.event.forward.ForwardProxyEventServiceFactory;
import spac.script.CSLApiImpl;
import spac.script.Commands;

import com.hyk.proxy.client.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.client.plugin.Plugin;

/**
 *
 */
public class Spac implements Plugin
{

	@Override
	public void onActive() throws Exception
	{
		CSL csl = CSL.Builder.build(getClass().getResourceAsStream("/spac.csl"));
		csl.setCalculator(new CSLApiImpl());
		csl.setComparator(new CSLApiImpl());
		csl.addFunction(Commands.INT);
		csl.addFunction(Commands.GETHEADER);
		csl.addFunction(Commands.PRINT);
		HttpProxyEventServiceFactory.Registry.register(new SpacProxyEventServiceFactory(csl));
		HttpProxyEventServiceFactory.Registry.register(new DirectProxyEventServiceFactory());
		HttpProxyEventServiceFactory.Registry.register(new ForwardProxyEventServiceFactory());
	}
	@Override
	public void onLoad() throws Exception
	{
		// TODO Auto-generated method stub

	}

}
