/**
 * 
 */
package com.baidu.beidou.cproplan.service.impl;

import java.util.List;

import com.baidu.beidou.cproplan.bo.CproPlan;
import com.baidu.beidou.cproplan.dao.CproPlanDao;
import com.baidu.beidou.cproplan.service.CproPlanMgr;

/**
 * @author zhangpeng
 * @version 1.0.0
 */
public class CproPlanMgrImpl implements CproPlanMgr {

	private CproPlanDao cproPlanDao;

	public Long countAllPlan() {
		return cproPlanDao.countAllPlan();
	}

	public List<CproPlan> getPlanInfo() {
		return cproPlanDao.findPlanInfo();
	}

	public List<CproPlan> findPlanInfoorderbyUserId() {
		return cproPlanDao.findPlanInfoOrderbyPlanId();
	}

	public CproPlanDao getCproPlanDao() {
		return cproPlanDao;
	}

	public void setCproPlanDao(CproPlanDao cproPlanDao) {
		this.cproPlanDao = cproPlanDao;
	}
}