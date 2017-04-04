/**
 * 
 */
package com.baidu.beidou.cprogroup.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baidu.beidou.cprogroup.bo.CproGroup;

/**
 * @author zhangpeng
 * @version 1.0.0
 */
public interface CproGroupMgr {

	/**
	 * 更新用户白名单
	 * 
	 * @param userid
	 * @param whiteSites
	 * @param whiteTrades
	 *            2012-12-17 created by kanghongwei
	 */
	public void resetUserWhiteConfig(Integer userid, List<Integer> whiteSites, List<Integer> whiteTrades);

	public void updateWhiteUsers(List<Integer> newUserList);
	
	public List<Integer> getNeedRestUserList(List<Integer> newUserList);

	public List<Integer> getBaiduWhiteSites(List<Integer> baiduCommonTradeList);

	public List<Integer> getBaiduWhiteTrades(List<Integer> baiduCommonTradeList);

	public void adjustSiteTradeSystem(CproGroup cproGroup, Map<Integer, Integer> siteMapping, Set<Integer> newFirstTradeList, Set<Integer> oldFirstTradeList);

}