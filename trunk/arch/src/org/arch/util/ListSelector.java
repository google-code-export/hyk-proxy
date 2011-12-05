/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ListSelector.java 
 *
 * @author yinqiwen [ 2010-5-22 | 11:24:52 AM ]
 *
 */
package org.arch.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 *
 */
public class ListSelector<T>
{
	protected ArrayList<T>		list;
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
		this.list = new ArrayList<T>(list);
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
	
	public synchronized T randomSelect()
	{
		if(list.isEmpty())
		{
			return null;
		}
		if(1 == list.size())
		{
			return list.get(0);
		}
		Random random = new Random();
		return list.get(random.nextInt(list.size()));
	}
	
	public synchronized void remove(T obj)
	{
		list.remove(obj);
	}
}
