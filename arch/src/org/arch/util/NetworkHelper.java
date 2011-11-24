/**
 * 
 */
package org.arch.util;

import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * @author wqy
 * 
 */
public class NetworkHelper
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
	
	public static long ipv42Int(String ip)
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
}
