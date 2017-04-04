package com.baidu.beidou.auditmanager.task.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.auditmanager.service.AuditUnitMgr;
import com.baidu.beidou.auditmanager.service.ResultSetCallBack;
import com.baidu.beidou.auditmanager.task.AuditUnitPatrolTask;
import com.baidu.beidou.auditmanager.vo.AuditCprounit;

public class AuditUnitPatrolTaskImpl implements AuditUnitPatrolTask {

	private static final Log log = LogFactory.getLog(AuditUnitPatrolTaskImpl.class);
	
	private AuditUnitMgr auditUnitMgr;
	
	private ResultSetCallBack<AuditCprounit> callBack;
	
	public int auditUnitPatrol(Date timeStart) {
		//分表查询待审创意，并且以用户为维度，组织用户创意信息发送到mq中(redis)
		log.info("deal with audit unit at:"+timeStart);
		int res = auditUnitMgr.getAndDealWithAuditUsersInfo(timeStart,callBack);
		return res;
	}
	
	public AuditUnitMgr getAuditUnitMgr() {
		return auditUnitMgr;
	}

	public void setAuditUnitMgr(AuditUnitMgr auditUnitMgr) {
		this.auditUnitMgr = auditUnitMgr;
	}

	public ResultSetCallBack<AuditCprounit> getCallBack() {
		return callBack;
	}

	public void setCallBack(ResultSetCallBack<AuditCprounit> callBack) {
		this.callBack = callBack;
	}

}
