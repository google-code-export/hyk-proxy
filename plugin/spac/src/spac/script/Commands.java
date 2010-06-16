/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Commands.java 
 *
 * @author yinqiwen [ 2010-6-15 | 08:04:39 PM ]
 *
 */
package spac.script;

import org.jboss.netty.handler.codec.http.HttpRequest;
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
			if(obj == null)
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
			HttpRequest req = (HttpRequest)arg0[0];
			String headername = (String)arg0[1];
			return req.getHeader(headername);
		}
	};
	
	public static final InvokeCommand PRINT = new InvokeCommand()
	{
		@Override
		public String getName()
		{
			return "print";
		}
		
		@Override
		public Object execute(Object[] arg0)
		{
			System.out.println(arg0[0]);
			return null;
		}
	};
}
