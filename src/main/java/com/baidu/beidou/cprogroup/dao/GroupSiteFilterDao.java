package com.baidu.beidou.cprogroup.dao;

import java.util.List;

import com.baidu.beidou.cprogroup.bo.GroupSiteFilter;

/**
 * 推广组站点过滤Dao层
 * 
 * @author zengyunfeng
 * @version 1.1.2
 */
public interface GroupSiteFilterDao {

	/**
	 * 根据推广组id查找对应的过滤站点
	 * 
	 * @param groupid
	 * @param userId
	 * @return 2012-12-14 created by kanghongwei
	 */
	public List<GroupSiteFilter> findByGroupId(final Integer groupid, int userId);
}
