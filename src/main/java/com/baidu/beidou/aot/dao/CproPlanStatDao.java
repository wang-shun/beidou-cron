package com.baidu.beidou.aot.dao;

import java.util.Date;
import java.util.List;

import com.baidu.beidou.aot.bo.PlanAotInfo;

public interface CproPlanStatDao {

	/**
	 * 获取全部推广计划信息
	 * 
	 * @param weekday
	 *            星期几：周日-周六：0-6
	 * @return
	 */
	public List<PlanAotInfo> findAllPlanInfo(int weekday);

}
