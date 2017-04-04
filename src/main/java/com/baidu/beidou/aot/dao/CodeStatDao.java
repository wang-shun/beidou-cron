package com.baidu.beidou.aot.dao;

import java.util.List;

import com.baidu.beidou.aot.bo.RegionCodeInfo;
import com.baidu.beidou.aot.bo.SiteTradeCodeInfo;

/**
 * 
 * @author kanghongwei
 * 
 *         refactor by kanghongwei since 2012-10-31
 */
public interface CodeStatDao {

	/**
	 * 获取全部地域信息
	 * 
	 * @return
	 */
	public List<RegionCodeInfo> findAllRegionCodeInfo();

	/**
	 * 获取全部网站行业信息
	 * 
	 * @return
	 */
	public List<SiteTradeCodeInfo> findAllSiteTradeInfo();
}
