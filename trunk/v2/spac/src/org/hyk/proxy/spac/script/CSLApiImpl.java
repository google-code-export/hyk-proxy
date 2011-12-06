/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: CalculatorImpl.java 
 *
 * @author yinqiwen [ 2010-6-15 | 07:58:39 PM ]
 *
 */
package org.hyk.proxy.spac.script;

import org.tykedog.csl.api.ApiAdapter;

/**
 *
 */
public class CSLApiImpl extends ApiAdapter
{
	@Override
	public boolean logicAnd(Object opra, Object oprb)
	{
		if(opra instanceof Boolean && oprb instanceof Boolean)
		{
			Boolean a = (Boolean)opra;
			Boolean b = (Boolean)oprb;
			return a && b;
		}
		return super.logicAnd(opra, oprb);
	}
	
	@Override
	public boolean logicOr(Object opra, Object oprb)
	{
		if(opra instanceof Boolean && oprb instanceof Boolean)
		{
			Boolean a = (Boolean)opra;
			Boolean b = (Boolean)oprb;
			return a || b;
		}
		return super.logicAnd(opra, oprb);
	}
	
	@Override
	public Object add(Object opra, Object oprb)
	{
		if(opra instanceof String || oprb instanceof String)
		{
			String a = opra.toString();
			String b = oprb.toString();
			return a+b;
		}
		return super.add(opra, oprb);
	}
	
	@Override
	public int compare(Object opra, Object oprb)
	{
		if(opra instanceof String && oprb instanceof String)
		{
			String a = (String)opra;
			String b = (String)oprb;
			a = a.toLowerCase();
			b = b.toLowerCase();
			return a.compareTo(b);
		}
		if(opra instanceof Integer && oprb instanceof Integer)
		{
			Integer a = (Integer)opra;
			Integer b = (Integer)oprb;
			return a.compareTo(b);
		}
		if(opra instanceof Boolean && oprb instanceof Boolean)
		{
			Boolean a = (Boolean)opra;
			Boolean b = (Boolean)oprb;
			return a.compareTo(b);
		}
		if(opra instanceof Comparable && oprb instanceof Comparable)
		{
			Comparable a = (Comparable)opra;
			Comparable b = (Comparable)oprb;
			return a.compareTo(b);
		}
		return 0;
	}
	
	@Override
	public boolean match(Object opra, Object oprb)
	{
		if(opra instanceof String && oprb instanceof String)
		{
			String a = (String)opra;
			String b = (String)oprb;
			a = a.toLowerCase();
			b = b.toLowerCase();
			return a.contains(b);
		}
		return false;
	}
}
