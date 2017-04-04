package com.baidu.beidou.auditmanager.task;

import java.util.Date;

public interface AuditUnitPatrolTask {

	/**
	 * 获取待审核的创意，把获取到的创意存入到redis的mq中;
	 * @return
	 */
	public int auditUnitPatrol(Date timeStart);
	
}
