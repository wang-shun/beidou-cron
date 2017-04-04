package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.AdSizeDao;
import com.baidu.beidou.unionsite.service.SiteConstantMgr;

/**
 * beidou全局配置加载类
 * 
 * @author zhangpeng
 * @version 1.0.0
 * 
 */
public class SiteConstantMgrImpl implements SiteConstantMgr {
	private static final Logger LOG = Logger
			.getLogger(SiteConstantMgrImpl.class);
	private static final String FILE = "siteblacklist.txt";

	private AdSizeDao adSizeDao = null;

	/**
	 * 从文件中加载配置项
	 * 
	 */
	public void loadConfFile() {

		InputStream is = null;
		BufferedReader reader = null;

		try {
			is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(FILE);
			reader = new BufferedReader(new InputStreamReader(is));

			String line = reader.readLine();
			for (; line != null; line = reader.readLine()) {
				if (!StringUtils.isEmpty(line.trim())) {
					SiteConstant.BLACK_SITE.add(line.trim());
				}
			}

		} catch (IOException e) {
			LOG.error("load config FILE error:SiteConstantMgrImpl", e);
		} finally {

			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LOG.error("close config FILE error:SiteConstantMgrImpl", e);
				}
			}

			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOG.error("close config FILE error:SiteConstantMgrImpl", e);
				}
			}
		}

		// 读取数据库，配置Ad_size
		SiteConstant.FIXED_AD_SIZE = adSizeDao.findSizeBySizeTypes(SiteConstant.fixedSizeTypes);
		SiteConstant.FLOW_AD_SIZE = adSizeDao.findSizeBySizeTypes(SiteConstant.flowSizeTypes);
		SiteConstant.FILM_AD_SIZE = adSizeDao.findSizeBySizeTypes(SiteConstant.filmSizeTypes);

	}

	/**
	 * @param adSizeDao
	 *            the adSizeDao to set
	 */
	public void setAdSizeDao(AdSizeDao adSizeDao) {
		this.adSizeDao = adSizeDao;
	}
}