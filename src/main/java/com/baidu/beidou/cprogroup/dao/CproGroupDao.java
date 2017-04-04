/**
 * 
 */
package com.baidu.beidou.cprogroup.dao;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.cprogroup.bo.CproGroupSimilarPeople;

/**
 * @author zhangpeng
 * @version 1.0.0
 * 
 *          refactor by kanghongwei since 2012-10-30
 */
public interface CproGroupDao {

	/**
	 * 更新推广组的行业投放字段
	 * 
	 * @param groupId
	 *            推广组ID
	 * @param siteTradeList
	 *            行业投放字段
	 */
	public void updateGroupInfoSiteTradeList(Integer groupId, String siteTradeList);

	/**
	 * 更新推广组的网站/行业设置
	 * 
	 * @param group
	 */
	public void updateCproGroup(CproGroup group);

	/**
	 * 更新定向投放网站个数(sitesum)
	 * 
	 * @param groupId
	 * @param siteSum
	 */
	public void updateGroupSiteSum(final int groupId, final int siteSum);

    /**
     * 根据推广组ID获取与相似人群有关的推广组信息
     * 
     * @param groupId 推广组ID
     * @return 推广组信息
     */
    public CproGroupSimilarPeople findCproGroupSimilarPeople(Integer groupId);

    /**
     * 修改推广组相似人群的开启状态
     * 
     * @param userId 用户ID
     * @param groupId 推广组ID
     * @param srcSimilarFlag 相似人群原始状态
     * @param destSimilarFlag 相似人群目标状态
     * @return 是否执行成功
     */
    public boolean modSimilarFlag(Integer userId, Integer groupId, int srcSimilarFlag, int destSimilarFlag);
    
    /**
     * 修改推广组SYS地域信息
     * @param userId 用户ID
     * @param groupId 推广组ID
     * @param sysRegionStr sys地域信息
     * @return 是否执行成功
     */
    public boolean updateGroupSysRegion(Integer userId, Integer groupId, String sysRegionStr);
}