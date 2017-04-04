package com.baidu.beidou.aot.dao;

import java.util.List;

import com.baidu.beidou.aot.bo.SiteAotInfo;

/**
 * 
 * @author kanghongwei
 * 
 *         refactor by kanghongwei since 2012-10-31
 */
public interface UnionSiteStatDao {

	/**
	 * 获取全库站点统计信息
	 * 
	 * @return
	 */
	public List<SiteAotInfo> findAllSiteAotInfo();
}
