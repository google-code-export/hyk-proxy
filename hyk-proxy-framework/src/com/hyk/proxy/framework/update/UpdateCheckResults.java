/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: UpdateCheckResults.java 
 *
 * @author qiying.wang [ May 17, 2010 | 3:07:37 PM ]
 *
 */
package com.hyk.proxy.framework.update;

import java.util.LinkedList;
import java.util.List;

import com.hyk.proxy.framework.common.Version;
import com.hyk.proxy.framework.plugin.PluginDescription;
import com.hyk.proxy.framework.plugin.PluginManager;
import com.hyk.proxy.framework.update.ProductReleaseDetail.FrameworkReleaseDetail;
import com.hyk.proxy.framework.update.ProductReleaseDetail.PluginReleaseDetail;
import com.hyk.proxy.framework.update.ProductUpdateDetail.CommonUpgradeDetail;
import com.hyk.proxy.framework.update.ProductUpdateDetail.PluginUpdateDetail;
import com.hyk.proxy.framework.util.VersionUtil;

/**
 *
 */
public class UpdateCheckResults
{
	public final ProductReleaseDetail releaseDetail;
	public final ProductUpdateDetail updateDetail;

	public UpdateCheckResults(ProductReleaseDetail detail,
	        ProductUpdateDetail updateDetail)
	{
		this.releaseDetail = detail;
		this.updateDetail = updateDetail;
	}

	public FrameworkReleaseDetail getNewerFrameworkRelease()
	{
		FrameworkReleaseDetail latest = releaseDetail.framework;
		if (!latest.version.equals(Version.value))
		{
			return latest;
		}
		return null;
	}

	public List<PluginReleaseDetail> getNewerPluginReleases()
	{
		PluginManager pm = PluginManager.getInstance();
		List<PluginReleaseDetail> all = releaseDetail.plugins;
		List<PluginReleaseDetail> ret = new LinkedList<PluginReleaseDetail>();
		for (PluginReleaseDetail plugin : all)
		{
			PluginDescription pd = pm.getPluginDescription(plugin.name);
			if (null != pd && !pd.version.equals(plugin.version))
			{
				ret.add(plugin);
			}
		}
		return ret;
	}

	public CommonUpgradeDetail getPluginUpdateDetail(PluginReleaseDetail plugin)
	{
		PluginManager pm = PluginManager.getInstance();
		String pluginVersion = pm.getPluginDescription(plugin.name).version;
		for (PluginUpdateDetail pd : updateDetail.plugins)
		{
			if (pd.name.equals(plugin.name))
			{
				if (VersionUtil
				        .match(pluginVersion, pd.detail.from, pd.detail.to))
				{
					return pd.detail;
				}
				break;
			}
		}
		return null;
	}

	public CommonUpgradeDetail getFrameworkUpdateDetail()
	{
		CommonUpgradeDetail all = updateDetail.framework.detail;
		if (VersionUtil.match(Version.value, all.from, all.to))
		{
			return all;
		}
		return null;
	}
}
