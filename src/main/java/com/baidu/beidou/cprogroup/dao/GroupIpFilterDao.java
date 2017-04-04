package com.baidu.beidou.cprogroup.dao;

import java.util.List;

import com.baidu.beidou.cprogroup.bo.GroupIpFilter;

/**
 * 推广组IP过滤Dao层
 * 
 * @author zengyunfeng
 * @version 1.1.2
 */
public interface GroupIpFilterDao {

	/**
	 * 根据推广组id查找对应的过滤ip
	 * 
	 * @param groupid
	 * @param userId
	 * @return 2012-12-16 created by kanghongwei
	 */
	public List<GroupIpFilter> findByGroupId(Integer groupid, int userId);
}
