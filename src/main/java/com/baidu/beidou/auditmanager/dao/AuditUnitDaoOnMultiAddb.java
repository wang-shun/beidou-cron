package com.baidu.beidou.auditmanager.dao;

import java.util.Date;

import com.baidu.beidou.auditmanager.service.ResultSetCallBack;
import com.baidu.beidou.auditmanager.vo.AuditCprounit;

public interface AuditUnitDaoOnMultiAddb {

	/**
	 * findAuditUserInfo: 查询待审核用户的信息，并作为审核任务推送
	 * @version cpweb-567
	 * @author wangxj
	 * @date May 10, 2013
	 */
	public int findAndDealWithAuditInfo(int[] excludeshifenstate, Date timeStart, ResultSetCallBack<AuditCprounit> callBack);


}
