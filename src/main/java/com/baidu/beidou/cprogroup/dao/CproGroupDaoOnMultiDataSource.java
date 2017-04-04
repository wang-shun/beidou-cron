/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource.java
 * 上午11:46:27 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.cprogroup.bo.CproGroupMoreInfo;
import com.baidu.beidou.cprogroup.bo.CproGroupRegion;
import com.baidu.beidou.indexgrade.bo.SimpleGroup;

/**
 * 
 * @author kanghongwei
 */

public interface CproGroupDaoOnMultiDataSource {

	/**
	 * 根据用户ID获取信息
	 * 
	 * @param userId
	 * @return 2012-12-16 created by kanghongwei
	 */
	public List<CproGroup> findGroupInfobyUserId(final Integer userId);

	/**
	 * 根据推广计划获取更多信息，包括：推广组ID，推广组名称，价格，网站、地域设置
	 * 
	 * @param planId
	 * @param userId
	 * @return 2012-12-16 created by kanghongwei
	 */
	public List<CproGroupMoreInfo> findEffectGroupInfoMorebyPlanId(final Integer planId, int userId);

	/**
	 * 获取所有推广组ID
	 * 
	 * @return 推广组ID列表
	 */
	public List<Integer> getAllCproGroupIds();

	/**
	 * 根据推广组ID，获取推广组的投放信息
	 * 
	 * @param groupId
	 *            推广组ID
	 * @return 推广组信息
	 */
	public CproGroup findGroupInfoByGroupId(Integer groupId);

	/**
	 * 获得推广组名称
	 * 
	 * @param groupId
	 * @param userId
	 * @return 2012-12-16 created by kanghongwei
	 */
	public String findGroupNameByGroupId(Integer groupId, int userId);

	/**
	 * 指定一批推广组IDs，过滤出设置全网投放的推广组IDS
	 * 
	 * @param groupIds
	 * @return下午02:47:01
	 */
	public List<Integer> filterGroupByAllSite(List<Integer> groupIds);

	/**
	 * 获取一个分片有效推广计划下的所有推广组的个数
	 * 
	 * @param sharding
	 *            当前数据库分片
	 * @return 2012-12-17 created by kanghongwei
	 */
	public Long countAllGroupIdofEffPlan(int sharding);

	/**
	 * 分页获取有效推广计划下的所有推广组【 由于全库分页效率比较底，这里分页在单库进行】
	 * 
	 * @param sharding
	 *            数据库的具体分片
	 * @param currPage
	 * @param pageSize
	 * @return 2012-12-17 created by kanghongwei
	 */
	public List<Integer> findAllGroupIdofEffPlan(int sharding, final int currPage, final int pageSize);

	/**
	 * 查询包含次地域定向的推广组概要信息
	 * 
	 * @param regions
	 * @return
	 */
	public List<Map<String, Object>> findGroupByRegId(final int regId);

	/**
	 * 根据用户ID获取全部groupId
	 * 
	 * @param userId
	 * @return
	 */
	public List<Integer> findGroupIdsByUserId(Integer userId);

	/**
	 * 根据指定的推广组IDs,获得它们所属的用户ID列表，非重复，用于权限验证
	 * 
	 * @param planIds
	 * @return
	 */
	public List<Integer> findUserIdByGroupIds(List<Integer> groupIds);

	/**
	 * 根据用户ID和定向方式获取全部groupId
	 * 
	 * @param userId
	 * @return
	 */
	public List<Integer> findGroupIdsByUserIdAndTargettype(int userId, int targetType);

	/**
	 * 根据groupId获得投放地域的idlist
	 * 
	 * @param groupId
	 * @param userId
	 * @return 2012-12-16 created by kanghongwei
	 */
	public List<Integer> getRegionList(int groupId, int userId);

	/**
	 * 根据groupId获得是否是全地域投放标志位
	 * 
	 * @param groupId
	 * @param userId
	 * @return 2012-12-16 created by kanghongwei
	 */
	public int getIsallregionTag(int groupId, int userId);

    /**
     * 删除无效的推广组del表数据
     */
    public void delInvalidGroupDelInfo();
    
    /**
     * 获取推广组地域信息
     * 
     * @param sharding DB分片
     * @return 地域信息
     */
    public List<CproGroupRegion> getGroupRegion(int sharding);
    
    /**
     * 查询所有price小于price参数且activity_state为0的推广组
     * @param price
     * @return
     */
    List<SimpleGroup> getAllGroupIdByPrice(int price);
}
