/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventService.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:14:39 AM ]
 *
 */
package org.hyk.proxy.spac.handler;

import org.arch.event.Event;
import org.arch.event.EventHeader;
import org.arch.event.NamedEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 */
public class SpacProxyEventHandler implements NamedEventHandler
{
	protected Logger logger = LoggerFactory.getLogger(getClass());


	
	@Override
    public void onEvent(EventHeader header, Event event)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public String getName()
    {
	    // TODO Auto-generated method stub
	    return "SPAC";
    }

}
