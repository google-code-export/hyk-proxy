/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: UpdateCheckResults.java 
 *
 * @author qiying.wang [ May 17, 2010 | 3:07:37 PM ]
 *
 */
package com.hyk.proxy.common.update;

import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.Version;
import com.hyk.proxy.common.update.ProductReleaseDetail;
import com.hyk.proxy.common.update.ProductReleaseDetail.ReleaseDetail;

/**
 *
 */
public class UpdateCheckResults
{
	public static final String ANNOUNCE = "A new version of hyk-proxy has been released!";
	public static final String NOTICE_FORMATTER = "Version %s is availbel at %s" + Constants.OFFICIAL_SITE + "%s"; 
	public static final String NOTICE_DOWNLOAD = "You can download the version from the diirect link:";
	
	public final ProductReleaseDetail detail;
	
	public UpdateCheckResults(ProductReleaseDetail detail)
	{
		this.detail = detail;
	}

	public ReleaseDetail getNewerRelease()
	{
		boolean isCurrentUnstable = false;
		if(Version.value.startsWith("nb"))
		{
			//you are using unstable release
			isCurrentUnstable = true;
		}
		
		if(isCurrentUnstable)
		{
			ReleaseDetail latest = detail.latestUnstableRelease;
			if(null == latest || null == latest.version)
			{
				latest = detail.stableRelease;
			}
			if(!latest.version.equals(Version.value))
			{
				return latest;
			}
		}
		else
		{
			//only compare stable for current stable version
			ReleaseDetail latest = detail.stableRelease;
			if(!latest.version.equals(Version.value))
			{
				return latest;
			}
		}
		return null;

	}
	
	
}
