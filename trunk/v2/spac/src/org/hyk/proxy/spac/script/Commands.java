/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Commands.java 
 *
 * @author yinqiwen [ 2010-6-15 | 08:04:39 PM ]
 *
 */
package org.hyk.proxy.spac.script;

import java.io.IOException;
import java.util.StringTokenizer;

import org.arch.event.http.HTTPMessageEvent;
import org.arch.event.http.HTTPResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tykedog.csl.api.InvokeCommand;

/**
 *
 */
public class Commands
{
	protected static Logger logger = LoggerFactory.getLogger(Commands.class);
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
			HTTPMessageEvent msg = (HTTPMessageEvent) arg0[0];
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
			HTTPResponseEvent res = (HTTPResponseEvent) arg0[0];
			return res.statusCode;
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
				StringTokenizer st = new StringTokenizer(arg0[0].toString());
				String[] cmdarray = new String[st.countTokens()];
				for (int i = 0; st.hasMoreTokens(); i++)
					cmdarray[i] = st.nextToken();
				ProcessBuilder builder = new ProcessBuilder(cmdarray);
				builder.redirectErrorStream(true);
				Process p = builder.start();

				byte[] b = new byte[4096];
				int ret = p.getInputStream().read(b);
				if (ret > 0)
				{
					return new String(b, 0, ret);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return null;
		}
	};

	public static final InvokeCommand LOG = new InvokeCommand()
	{
		@Override
		public String getName()
		{
			return "log";
		}

		@Override
		public Object execute(Object[] arg0)
		{
			logger.info(arg0[0].toString());
			return null;
		}
	};
}
