/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ListUsers.java 
 *
 * @author yinqiwen [ 2010-4-9 | 08:49:51 PM]
 *
 */
package org.hyk.proxy.gae.client.admin.handler;

import java.util.List;

import org.arch.event.Event;
import org.arch.event.EventHandler;
import org.arch.event.EventHeader;
import org.hyk.proxy.gae.client.admin.GAEAdmin;
import org.hyk.proxy.gae.client.connection.ProxyConnection;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.auth.Group;
import org.hyk.proxy.gae.common.auth.User;
import org.hyk.proxy.gae.common.event.AdminResponseEvent;
import org.hyk.proxy.gae.common.event.ListGroupRequestEvent;
import org.hyk.proxy.gae.common.event.ListGroupResponseEvent;
import org.hyk.proxy.gae.common.event.ListUserRequestEvent;
import org.hyk.proxy.gae.common.event.ListUserResponseEvent;


/**
 *
 */
public class ListGroups implements CommandHandler
{
	public static final String COMMAND = "groups";
	
	private ProxyConnection connection;
	public ListGroups(ProxyConnection connection)
	{
		this.connection = connection;
	}
	
	@Override
	public void execute(String[] args)
	{
		ListGroupRequestEvent event = new ListGroupRequestEvent();
		final String formater = "%20s%20s";
		String header = String.format(formater,  "Group", "Blacklist");
		GAEAdmin.outputln(header);
		EventHandler handler = new EventHandler()
		{
			@Override
			public void onEvent(EventHeader header, Event event)
			{
				synchronized (this)
				{
					this.notify();
				}
				if (header.type == GAEConstants.GROUOP_LIST_RESPONSE_EVENT_TYPE)
				{
					ListGroupResponseEvent res = (ListGroupResponseEvent) event;
					for(Group line:res.groups)
					{
						String output = String.format(formater, line.getName(), line.getBlacklist());
						GAEAdmin.outputln(output);
					}
				}
				else if (event instanceof AdminResponseEvent)
				{
					AdminResponseEvent ev = (AdminResponseEvent) event;
					GAEAdmin.outputln(ev.response != null ? ev.response
					        : ev.errorCause);
				}
			}
		};
		if (connection.send(event, handler))
		{
			synchronized (handler)
			{
				try
				{
					handler.wait(60 * 1000);
				}
				catch (InterruptedException e)
				{

				}
			}
		}
	}

	@Override
	public void printHelp()
	{
		// TODO Auto-generated method stub
		
	}

}
