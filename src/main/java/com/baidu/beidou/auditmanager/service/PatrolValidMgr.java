package com.baidu.beidou.auditmanager.service;

import java.util.Date;
import java.util.List;

import com.baidu.beidou.auditmanager.vo.AkaAuditUnit;
import com.baidu.beidou.auditmanager.vo.AkaUnitForMail;
import com.baidu.beidou.auditmanager.vo.IllegalUnit;
import com.baidu.beidou.auditmanager.vo.Unit;
import com.baidu.beidou.util.akadriver.bo.AkaBeidouResult;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;

/**
 * ClassName: PatrolValidMgr
 * Function: TODO ADD FUNCTION
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 
 * @since TODO
 * @date Aug 4, 2011
 * @see 
 */
public interface PatrolValidMgr {
	/**
	 * findValidUnitList: 从数据库表中获取符合aka轮询的有效广告
	 * @version PatrolValidDao
	 * @author genglei01
	 * @date Aug 3, 2011
	 */
	public List<AkaAuditUnit> findValidUnitList(int tableIndex);
	
	//add userid by chongjie since 20121204 for cpweb-535
	public void auditRefuse(Date startTime, List<AkaAuditUnit> akaUnitList, 
			List<AkaBeidouResult> resultText, List<Unit> resUnits, Integer userId);
	
	public void loadRefuseReasonMap();
	
	public void sendMail(Integer userId, List<AkaUnitForMail> auditRecordList) throws Exception;
	
	public PartitionStrategy getStrategy();
}
