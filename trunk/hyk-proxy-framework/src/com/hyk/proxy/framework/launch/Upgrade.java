/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Upgrade.java 
 *
 * @author yinqiwen [ 2010-8-19 | 12:36:38 PM ]
 *
 */
package com.hyk.proxy.framework.launch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.hyk.proxy.framework.common.Constants;
import com.hyk.util.io.FileUtil;

/**
 *
 */
public class Upgrade
{
	public static final File UPGRADE_FILE = new File(
	        System.getProperty("user.home")
	                + System.getProperty("file.separator") + ".hyk-proxy"
	                + System.getProperty("file.separator") + ".upgrade_detail");
	public static final File UPGRADE_TRACK = new File(
	        System.getProperty("user.home")
	                + System.getProperty("file.separator") + ".hyk-proxy"
	                + System.getProperty("file.separator") + ".upgrade_track");

	public static final String UPGRADE_HOME = System.getProperty("user.home")
	        + System.getProperty("file.separator") + Constants.PREF_HOME
	        + System.getProperty("file.separator") + ".update"
	        + System.getProperty("file.separator");
	private static final String BLANK = " ";

	public static enum UpgradeAction
	{
		REPLACE, REMOVE, ADD
	}

	private static StringBuilder upgradeDetailBuffer = new StringBuilder();

	public static boolean upgrade()
	{
		if (!UPGRADE_FILE.exists())
		{
			return true;
		}
		return doUpgrade();
	}

	public static void appendReplaceDetail(String zipName, String fileDetail)
	{
		upgradeDetailBuffer.append(UpgradeAction.REPLACE).append(BLANK)
		        .append(zipName).append(BLANK).append(fileDetail)
		        .append(System.getProperty("line.separator"));
	}

	public static void appendRemoveDetail(String fileDetail)
	{
		upgradeDetailBuffer.append(UpgradeAction.REMOVE).append(BLANK)
		        .append(fileDetail)
		        .append(System.getProperty("line.separator"));
	}

	public static void flushUpgradeDetails() throws IOException
	{
		String info = upgradeDetailBuffer.toString();
		if (!info.isEmpty())
		{
			BufferedWriter upgrade = new BufferedWriter(new FileWriter(
			        UPGRADE_FILE, true));
			upgrade.write(info);
			upgradeDetailBuffer = new StringBuilder();
			upgrade.close();
		}
	}

	private static boolean doUpgrade()
	{
		try
		{
			String home = System.getProperty("HYK_PROXY_HOME");
			String userHome = System.getProperty("user.home");
			String filesep = System.getProperty("file.separator");
			BufferedReader reader = new BufferedReader(new FileReader(
			        UPGRADE_FILE));
			BufferedWriter track = new BufferedWriter(new FileWriter(
			        UPGRADE_TRACK, true));
			String line = null;
			while (null != (line = reader.readLine()))
			{
				line = line.trim();
				if (line.startsWith("#"))
				{
					continue;
				}
				String[] details = line.split(BLANK);
				UpgradeAction action = Enum.valueOf(UpgradeAction.class,
				        details[0].trim());

				switch (action)
				{
					case ADD:
					case REPLACE:
					{
						String zipFileName = details[1].trim();
						String entryPath = details[2].trim();
						String targetPath = entryPath;

						ZipFile zipFile = new ZipFile(new File(UPGRADE_HOME,
						        zipFileName));

						//framework
						if (targetPath.startsWith("hyk-proxy-"))
						{
							int index = targetPath.indexOf("/");
							targetPath = targetPath.substring(index + 1);
						}
						else
						{
							int index = entryPath.indexOf("/");
							entryPath = entryPath.substring(index + 1);
						}
						ZipEntry entry = new ZipEntry(entryPath);
						InputStream fis = zipFile.getInputStream(entry);
						if(null == fis)
						{
							track.write("Error: " + entryPath + " is null in zip file:" + zipFileName);
							continue;
						}
						FileOutputStream fos = null;
						// user plugin
						if (targetPath.startsWith("."))
						{
							fos = new FileOutputStream(userHome + filesep
							        + ".hyk-proxy" + filesep
							        + targetPath.replace("/", filesep));
						}
						else
						{
							File dest = new File(home + filesep
							        + targetPath.replace("/", filesep));

							if (!FileUtil.canWrite(dest))
							{
								dest = FileUtil.createFile(UPGRADE_HOME
								        + filesep
								        + targetPath.replace("/", filesep));
							}
							else
							{
								dest = FileUtil.createFile(home + filesep
								        + targetPath.replace("/", filesep));
							}
							fos = new FileOutputStream(dest);
						}

						try
						{
							doCopy(fis, fos);
						}
						finally
						{
							fos.close();
							// fis.close();
						}
						break;
					}
					case REMOVE:
					{
						String entryPath = details[1].trim();
						if (entryPath.startsWith("."))
						{
							FileUtil.deleteFile(new File(userHome + filesep
							        + ".hyk-proxy" + filesep + entryPath));
						}
						else
						{
							FileUtil.deleteFile(new File(home + filesep
							        + entryPath));
						}
						break;
					}
				}

				track.write("Upgrade " + line + " success!"
				        + System.getProperty("line.separator"));
			}
			reader.close();
			UPGRADE_FILE.delete();
			track.write("Upgrade success!"
			        + System.getProperty("line.separator"));
			track.close();
			return true;
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return false;
	}

	private static long doCopy(InputStream input, OutputStream output)
	        throws IOException
	{
		byte[] buffer = new byte[4096];
		long count = 0;
		int n = 0;
		while (null != input && -1 != (n = input.read(buffer)))
		{
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
