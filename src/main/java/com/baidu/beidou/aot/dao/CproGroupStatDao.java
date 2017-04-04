package com.baidu.beidou.aot.dao;

import java.util.List;

import com.baidu.beidou.aot.bo.GroupAotInfo;

/**
 * 
 * @author kanghongwei
 * 
 *         2012-10-31
 */
public interface CproGroupStatDao {
	/**
	 * 仅获取推广组基准price信息
	 * 
	 * @return
	 */
	public List<GroupAotInfo> findAllGroupAotInfoOnlyPrice();

	/**
	 * 获取推广组当前全部信息，不包含price信息
	 * 
	 * @param curPage
	 * @param pageSize
	 * @return 2012-12-17 created by kanghongwei
	 */
	public List<GroupAotInfo> findGroupAotInfoByPage(int curPage, int pageSize);

}
