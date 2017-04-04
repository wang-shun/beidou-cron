/**
 * 
 */
package com.baidu.beidou.cproplan.service;

import java.util.List;

import com.baidu.beidou.cproplan.bo.CproPlan;

/**
 * @author zhangpeng
 * @version 1.0.0
 */
public interface CproPlanMgr {

	/**
	 * 获取总的推广组个数
	 * 
	 * @return上午10:51:32
	 */
	public Long countAllPlan();

	public List<CproPlan> getPlanInfo();

	/**
	 * 获取信息,包括推广计划ID，推广计划名称，userid
	 * 
	 * @return 2013-1-10 created by kanghongwei
	 */
	public List<CproPlan> findPlanInfoorderbyUserId();
}