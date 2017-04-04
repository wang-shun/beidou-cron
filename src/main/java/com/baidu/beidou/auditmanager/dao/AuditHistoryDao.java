package com.baidu.beidou.auditmanager.dao;

import java.io.BufferedWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import com.baidu.beidou.auditmanager.vo.AuditHistoryView;


/**
 * 审核历史Dao
 * @author zhangpeng
 * @version 1.1.0
 */
public interface AuditHistoryDao {	
	
	/**
	 * 查询审核时间在$startTime之后的审核历史，<br>
	 * 如果拒绝理由在监控集合中，则输出用户和对应的拒绝理由
	 * @author zengyunfeng
	 * @param output 输出流
	 * @param monitorReason 监控集合
	 * @param startTime 
	 */
	public boolean findAuditHistoryAndOutputMonitorFile(BufferedWriter output, Set<String> monitorReason, Calendar startTime );
	
	/**
	 * findNotSyncAuditHistory: 获取未同步到ubmc的审核历史
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<AuditHistoryView> findNotSyncAuditHistory(int maxMaterNum);
	
	/**
	 * updateAuditHistory: 更新审核历史mcId、mcVersionId以及已同步字段
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateAuditHistory(Integer id, Long mcId, Integer mcVersionId, Integer userId);
}