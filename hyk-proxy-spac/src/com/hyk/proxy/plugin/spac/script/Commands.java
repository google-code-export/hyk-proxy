/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Commands.java 
 *
 * @author yinqiwen [ 2010-6-15 | 08:04:39 PM ]
 *
 */
package com.hyk.proxy.plugin.spac.script;

import java.io.IOException;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.tykedog.csl.api.InvokeCommand;

/**
 *
 */
public class Commands
{
	public static final InvokeCommand INT = new InvokeCommand()
	{
		@Override
		public String getName()
		{
			return "int";
		}

		@Override
		public Object execute(Object[] arg0)
		{
			Object obj = arg0[0];
			if (obj == null)
			{
				return 0;
			}
			String o = obj.toString();
			return Integer.valueOf(o);
		}
	};

	public static final InvokeCommand GETHEADER = new InvokeCommand()
	{

		@Override
		public String getName()
		{
			return "getHeader";
		}

		@Override
		public Object execute(Object[] arg0)
		{
			HttpMessage msg = (HttpMessage) arg0[0];
			String headername = (String) arg0[1];
			return msg.getHeader(headername);
		}
	};

	public static final InvokeCommand GETRESCODE = new InvokeCommand()
	{

		@Override
		public String getName()
		{
			return "getResponseCode";
		}

		@Override
		public Object execute(Object[] arg0)
		{
			HttpResponse res = (HttpResponse) arg0[0];
			return res.getStatus().getCode();
		}
	};

	public static final InvokeCommand PRINT = new InvokeCommand()
	{
		@Override
		public String getName()
		{
			return "system";
		}

		@Override
		public Object execute(Object[] arg0)
		{
			System.out.println(arg0[0]);

			return null;
		}
	};

	public static final InvokeCommand SYSTEM = new InvokeCommand()
	{
		@Override
		public String getName()
		{
			return "system";
		}

		@Override
		public Object execute(Object[] arg0)
		{
			try
			{
				Runtime.getRuntime().exec(arg0[0].toString());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return null;
		}
	};
}
