package com.baidu.beidou.cprogroup.dao;

import java.util.List;

import com.baidu.beidou.cprogroup.bo.GroupSitePrice;

/**
 * 推广组分站点价格Dao层
 * 
 * @author zengyunfeng
 * @version 1.1.2
 */
public interface GroupSitePriceDao {

	/**
	 * 根据推广组id查找对应的分站点价格设置
	 * 
	 * @param groupid
	 * @param userId
	 * @return 2012-12-13 created by kanghongwei
	 */
	public List<GroupSitePrice> findByGroupId(Integer groupid, int userId);
}
