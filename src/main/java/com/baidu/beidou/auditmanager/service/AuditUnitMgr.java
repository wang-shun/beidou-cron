package com.baidu.beidou.auditmanager.service;

import java.util.Date;

import com.baidu.beidou.auditmanager.vo.AuditCprounit;

public interface AuditUnitMgr {

	/**
	 * 查询所有待审核创意，根据用户汇总,并用ResultSetCallBack处理查询的结果,返回用户数量
	 * @return
	 */
	public int getAndDealWithAuditUsersInfo(Date timeStart, ResultSetCallBack<AuditCprounit> callBack);

}
