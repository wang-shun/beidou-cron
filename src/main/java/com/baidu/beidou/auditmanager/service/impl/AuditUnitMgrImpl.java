package com.baidu.beidou.auditmanager.service.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.auditmanager.dao.AuditUnitDaoOnMultiAddb;
import com.baidu.beidou.auditmanager.service.AuditUnitMgr;
import com.baidu.beidou.auditmanager.service.ResultSetCallBack;
import com.baidu.beidou.auditmanager.vo.AuditCprounit;
import com.baidu.beidou.user.constant.UserConstant;

public class AuditUnitMgrImpl implements AuditUnitMgr {

	private static final Log log = LogFactory.getLog(AuditUnitMgrImpl.class);
	private AuditUnitDaoOnMultiAddb auditUnitDao;
	
	public int getAndDealWithAuditUsersInfo(Date timeStart, ResultSetCallBack<AuditCprounit> callBack) {
		if(callBack==null){
			log.warn("null callback,can not deal with resultset");
			return 0;
		}
		int auditUnitsNum = auditUnitDao.findAndDealWithAuditInfo(excludeShifenState,timeStart, callBack);
		return auditUnitsNum;
	}

	// 审核（一审）过滤条件
		private static final int[] excludeShifenState = new int[] {
				UserConstant.SHIFEN_STATE_CLOSE, UserConstant.SHIFEN_STATE_REFUSE,
				UserConstant.SHIFEN_STATE_DISABLE, UserConstant.SHIFEN_STATE_AUDITING };

		public AuditUnitDaoOnMultiAddb getAuditUnitDao() {
			return auditUnitDao;
		}

		public void setAuditUnitDao(AuditUnitDaoOnMultiAddb auditUnitDao) {
			this.auditUnitDao = auditUnitDao;
		}
		
		
}
