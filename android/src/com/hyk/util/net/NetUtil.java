/**
 * 
 */
package com.hyk.util.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

/**
 * @author wqy
 * 
 */
public class NetUtil
{
	public static boolean isIPV6Address(String address)
	{
		try
		{
			return InetAddress.getByName(address) instanceof Inet6Address;
		}
		catch(Throwable e)
		{
			return false;
		}
	}
	
	public static long  ipv42Int(String ip)
	{
		String[] addrArray = ip.split("\\.");

		long  num = 0;
		for (int i = 0; i < addrArray.length; i++)
		{
			int power = 3 - i;

			num += ((Integer.parseInt(addrArray[i]) % 256 * Math
			        .pow(256, power)));
		}
		return num;
	}

	public static String int2Ipv4(long  i)
	{
		return ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
		        + ((i >> 8) & 0xFF) + "." + (i & 0xFF);
	}

	/**
	 * 10.0.0.0 ~ 10.255.255.255
	 * 172.16.0.0 ~ 172.31.255.255
	 * 192.168.0.0 ~ 192.168.255.255
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean isPrivateIP(String ip)
	{
		try
        {
			if(!Character.isDigit(ip.charAt(0)))
			{
				return false;
			}
			long  value = ipv42Int(ip);
			if ((value >= 0x0A000000l && value <= 0x0AFFFFFFl)
			        || (value >= 0xAC100000l && value <= 0xAC1FFFFFl)
			        || (value >= 0xC0A80000l && value <= 0xC0A8FFFFl)

			)
			{
				return true;
			}
			return false;
        }
        catch (Exception e)
        {
	        return false;
        }
		
	}
	public static File downloadFile(URL url, File path) throws IOException
	{
		return downloadFile(null, url, path);
	}
	
	public static File downloadFile(Proxy proxy, URL url, File path) throws IOException
	{
		URLConnection conn = null;
		if(null == proxy)
		{
			conn = url.openConnection();
		}
		else
		{
			conn = url.openConnection(proxy);
		}
		conn.connect();
		
		File destFile = null;
		if(conn instanceof HttpURLConnection)
		{
			HttpURLConnection hc = (HttpURLConnection) conn;
			String filename = null;
			String hv = hc.getHeaderField("Content-Disposition");
			if(null == hv)
			{
				String str = url.toString();
				int index = str.lastIndexOf("/");
				filename = str.substring(index + 1);
			}
			else
			{
				int index = hv.indexOf("filename=");
				filename = hv.substring(index).replace("\"", "").trim();
			}
			
			destFile = new File(path, filename);
		}
		else
		{
			destFile = new File(path, "downloadfile" + url.toString().hashCode());
		}
		if(destFile.exists())
		{
			return destFile;
		}
		FileOutputStream fos = new FileOutputStream(destFile);
		byte[] buffer = new byte[2048];
		try
        {
			while(true)
			{
				int len = conn.getInputStream().read(buffer);
				if(len < 0)
				{
					break;
				}
				else
				{
					fos.write(buffer, 0, len);
				}
			}
			fos.close();
        }
        catch (IOException e)
        {
        	destFile.delete();
	        throw e;
        }
		
		return destFile;
	}
	
	public static void main(String[] args) throws Exception
	{
		//System.out.println(isPrivateIP("192.168.1.1"));
	}
}
