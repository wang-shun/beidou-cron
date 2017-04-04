package com.baidu.beidou.aot.dao;

import java.util.Date;
import java.util.List;

import com.baidu.beidou.aot.bo.PlanAotInfo;

public interface CproPlanOfflineStatDao {

	/**
	 * 从cproplan_offline表中获取当天最晚下线时间，如果不存在则返回null
	 * 
	 * @param planId
	 * @param from
	 * @param to
	 * @return
	 */
	public Date findLastOfftimeByDate(int planId, Date from, Date to, Integer userId);

}
