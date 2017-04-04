/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.facade.CproGroupFacade.java
 * 下午6:46:36 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.facade;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author kanghongwei
 */

public interface CproGroupFacade {

	/**
	 * 生成网站推广计划信息接口文件
	 */
	public void checkRepeateGroup();

	/**
	 * 更新用户白名单
	 * 
	 * @param newUserList
	 */
	public void updateUserWhiteList(List<Integer> newUserList);

	/**
	 * 网站分类体系变更后，对用户推广组定向投放的调整
	 * 
	 * @param siteMapping
	 *            新旧行业ID映射关系
	 * @param newFirstTradeList
	 *            新的全部一级行业ID列表（包括“其他”）
	 * @param oldFirstTradeList
	 *            旧的全部一级行业ID列表（包括“其他”）
	 */
	public void adjustSiteTradeSystem(Map<Integer, Integer> siteMapping, Set<Integer> newFirstTradeList, Set<Integer> oldFirstTradeList);

}
