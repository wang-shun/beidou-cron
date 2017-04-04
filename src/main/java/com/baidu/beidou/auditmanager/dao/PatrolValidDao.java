package com.baidu.beidou.auditmanager.dao;

import java.util.List;

import com.baidu.beidou.auditmanager.vo.AkaAuditUnit;
import com.baidu.beidou.auditmanager.vo.Unit;
import com.baidu.beidou.auditmanager.vo.UnitAuditing;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;

/**
 * ClassName: PatrolValidDao Function: 北斗轮询Dao
 * 
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version beidou-cron 1.1.2
 * @since TODO
 * @date Aug 3, 2011
 * @see
 */
public interface PatrolValidDao {

	public PartitionStrategy getStrategy();

	/**
	 * findValidUnitList: 从数据库表中获取符合aka轮询的有效广告
	 * 
	 * @version PatrolValidDao
	 * @author genglei01
	 * @date Aug 3, 2011
	 */
	public List<AkaAuditUnit> findValidUnitList(int tableIndex);

	public List<Unit> findUnits(List<Integer> userIds, List<Long> unitIds);

	public Unit findUnitById(int userId, Long unitId);

	public void updateUnit(int userId, Unit unit);

	public void addAuditing(int userId, UnitAuditing auditRea);
	
    /**
     * Function: 删除已上线的版本
     * 
     * @author genglei01
     * @param userId userId
     * @param unitId unitId
     */
	public void deleteOnlineUnit(int userId, Long unitId);
}
