/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SeattleProxyEventService.java 
 *
 * @author qiying.wang [ May 21, 2010 | 10:14:39 AM ]
 *
 */
package org.hyk.proxy.spac.handler.forward;

import org.arch.event.Event;
import org.arch.event.EventHeader;
import org.arch.event.NamedEventHandler;


/**
 *
 */
public class DirectProxyEventHandler implements NamedEventHandler
{
	@Override
    public void onEvent(EventHeader header, Event event)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public String getName()
    {
	    return "DIRECT";
    }


}
