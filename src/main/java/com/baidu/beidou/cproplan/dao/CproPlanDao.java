/**
 * 
 */
package com.baidu.beidou.cproplan.dao;

import java.util.List;

import com.baidu.beidou.cproplan.bo.CproPlan;

/**
 * @author zhangpeng
 * @version 1.0.0
 */
public interface CproPlanDao {

	/**
	 * 获取总的推广组个数
	 * 
	 */
	public Long countAllPlan();

	/**
	 * 分页获取信息
	 * 
	 */
	public List<CproPlan> findPlanInfo();

	/**
	 * 获取信息,包括推广计划ID，推广计划名称，userid
	 * 
	 * @return 2013-1-10 created by kanghongwei
	 */
	public List<CproPlan> findPlanInfoOrderbyPlanId();

    /**
     * 删除无效的推广计划del表数据
     */
    public void delInvalidPlanDelInfo();
}