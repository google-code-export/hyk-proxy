/**
 * This file is part of the hyk-util project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: FileUtil.java 
 *
 * @author yinqiwen [ 2010-8-15 | 04:37:31 PM]
 *
 */
package org.arch.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 *
 */
public class FileHelper
{
	public static boolean canWrite(File file)
	{
		if(file.isFile())
		{
			try
            {
				if(file.canWrite())
				{
					FileOutputStream fos = new FileOutputStream(file, true);
					fos.close();
		            return true;
				}
            }
            catch (Exception e)
            {
	            // TODO: handle exception
            }
			return false;
		}
		else
		{
			File test = new File(file, ".test");
			try
            {
				if(test.createNewFile())
				{
					test.delete();
					return true;
				}
				return false;		
            }
            catch (Exception e)
            {
	            return false;
            }
		}
	}
	
	public static File createFile(String path) throws IOException
	{
		File file = new File(path);
		if(file.exists())
		{
			return file;
		}
		if(!file.getParentFile().exists())
		{
			createDir(file.getParentFile().getAbsolutePath());
		}
		file.createNewFile();
		return file;
	}
	
	public static File createDir(String path)
	{
		File file = new File(path);
		if(file.exists())
		{
			return file;
		}
		if(!file.getParentFile().exists())
		{
			createDir(file.getParentFile().getAbsolutePath());
			
		}
		file.mkdir();
		return file;
	}
	
	public static boolean deleteFile(File file)
	{
		boolean success = true;
		if (file.isDirectory())
		{
			String[] children = file.list();
			for (int i = 0; i < children.length; i++)
			{
				 success = (deleteFile(new File(file, children[i])) && success);
			}
		}
		return  file.delete() && success;
	}
	
	public static void unzip(File srcFile, File destDir)throws ZipException, IOException
	{
		unzip(srcFile, destDir, true);
	}
	
	
	public static void unzip(File srcFile, File destDir, boolean replace) throws ZipException, IOException
	{
		int BUFF_SIZE = 2048;
		ZipFile zipFile = new ZipFile(srcFile);
		Enumeration emu = zipFile.entries();
		byte data[] = new byte[BUFF_SIZE];
		while (emu.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry) emu.nextElement();
			File file = new File(destDir.getAbsolutePath()
			        + System.getProperty("file.separator") + entry.getName());
			if(file.exists() && !replace)
			{
				throw new IOException("File exsit.");
			}
			if(file.exists() && replace)
			{
				deleteFile(file);
			}
			if (entry.isDirectory())
			{
				file.mkdirs();
				continue;
			}
			BufferedInputStream bis = new BufferedInputStream(
			        zipFile.getInputStream(entry));
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos, BUFF_SIZE);

			int count;

			while ((count = bis.read(data, 0, BUFF_SIZE)) != -1)
			{
				bos.write(data, 0, count);
			}
			bos.flush();
			bos.close();
			bis.close();
		}
		zipFile.close();
	}

}
