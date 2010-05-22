/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ListSelector.java 
 *
 * @author yinqiwen [ 2010-5-22 | 11:24:52 AM ]
 *
 */
package com.hyk.proxy.client.util;

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
		Collections.shuffle(list);
		this.list = list;
	}
	
	public synchronized T select()
	{
		if(cursor >= list.size())
		{
			cursor = 0;
		}
		return list.get(cursor++);
	}
}
