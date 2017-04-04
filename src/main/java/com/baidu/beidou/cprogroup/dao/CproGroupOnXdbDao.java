/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.CproGroupOnXdbDao.java
 * 下午12:58:11 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao;

import java.util.List;

/**
 * 
 * @author kanghongwei
 */

public interface CproGroupOnXdbDao {

	/**
	 * 根据网站ID和行业ID计算定向投放网站个数（sitesum）
	 * 
	 * @param groupId
	 * @param siteList
	 * @param tradeList
	 * @return 2012-10-30 created by kanghongwei
	 */
	public int calculateSiteSum(final int groupId, List<Integer> siteList, List<Integer> tradeList);

}
