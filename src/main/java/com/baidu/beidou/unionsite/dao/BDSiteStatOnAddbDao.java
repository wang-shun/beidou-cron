/**
 * beidou-cron-trunk#com.baidu.beidou.unionsite.dao.BDSiteStatOnAddbDao.java
 * 下午8:57:18 created by kanghongwei
 */
package com.baidu.beidou.unionsite.dao;

import java.util.List;

import com.baidu.beidou.unionsite.vo.UserSiteVO;

/**
 * 
 * @author kanghongwei
 * 
 *         2012-10-31
 */

public interface BDSiteStatOnAddbDao {

	/**
	 * 获得具有有效推广组的用户个数，用于对站点进行分批计算热度
	 * 
	 * @return 2012-10-29 created by kanghongwei
	 */
	int getAvailUserCount();

	/**
	 * 获得用户投放选择的站点记录信息
	 * 
	 * @return 2012-10-29 created by kanghongwei
	 */
	public List<UserSiteVO> statSiteUserVo();

	/**
	 * 获得全站点投放的用户列表
	 * 
	 * @return 2012-10-29 created by kanghongwei
	 */
	List<Integer> findAllSiteUser();

}
