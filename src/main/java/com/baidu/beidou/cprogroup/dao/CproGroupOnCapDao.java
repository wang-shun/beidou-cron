/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.CproGroupDaoOnCap.java
 * 下午12:43:11 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao;

import java.util.Map;

/**
 * 
 * @author kanghongwei
 */

public interface CproGroupOnCapDao {

	/**
	 * 获取一二级地域关系map（key为一级地域或二级地域id，value为一级地域id；当key为一级地域id时，value和key相同
	 * 
	 * @return 2012-10-30 created by kanghongwei
	 */
	public Map<Integer, Integer> getRegRelationMap();

	/**
	 * 获得地域ID和地域名称
	 * 
	 * @return 2012-10-30 created by kanghongwei
	 */
	public Map<Integer, String> getRegIdNameMap();

}
