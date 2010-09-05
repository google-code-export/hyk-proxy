/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SwingHelper.java 
 *
 * @author yinqiwen [ 2010-8-29 |07:04:15 PM]
 *
 */
package com.hyk.proxy.framework.util;

import java.util.Arrays;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 *
 */
public class SwingHelper
{
	public static void showBusyButton(Future runninttask, JButton button, String busytext)
	{
		ListSelector<ImageIcon> busys = new ListSelector<ImageIcon>(Arrays.asList(ImageUtil.BUSY_ICONS), false);
		while(true)
		{
			if(runninttask.isDone())
			{
				break;
			}
			button.setIcon(busys.select());
			button.setText(busytext);
			try
            {
	            Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
	            e.printStackTrace();
            }
		}
	}
}
