/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ListSelector.java 
 *
 * @author yinqiwen [ 2010-5-22 | 11:24:52 AM ]
 *
 */
package org.hyk.proxy.framework.util;

import java.util.Collections;
import java.util.List;


/**
 *
 */
public class ListSelector<T>
{
	protected List<T>		list;
	protected int						cursor;

	public ListSelector(List<T> list)
	{
		this(list, true);
	}
	
	public ListSelector(List<T> list, boolean shuffle)
	{
		if(shuffle)
		{
			Collections.shuffle(list);
		}
		this.list = list;
	}
	
	public synchronized T select()
	{
		if(list.isEmpty())
		{
			return null;
		}
		if(cursor >= list.size())
		{
			cursor = 0;
		}
		return list.get(cursor++);
	}
	
	public synchronized void remove(T obj)
	{
		list.remove(obj);
	}
}
