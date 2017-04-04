/**
 * beidou-cron-trunk#com.baidu.beidou.auditmanager.dao.impl.PatrolValidDaoImpl.java
 * 上午11:21:34 created by kanghongwei
 */
package com.baidu.beidou.auditmanager.dao.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.auditmanager.dao.PatrolValidDao;
import com.baidu.beidou.auditmanager.dao.rowmap.AkaAuditUnitRowMapping;
import com.baidu.beidou.auditmanager.dao.rowmap.UnitRowMapping;
import com.baidu.beidou.auditmanager.vo.AkaAuditUnit;
import com.baidu.beidou.auditmanager.vo.Unit;
import com.baidu.beidou.auditmanager.vo.UnitAuditing;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;
import com.baidu.beidou.util.partition.PartID;
import com.baidu.beidou.util.partition.impl.PartKeyBDidImpl;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;

/**
 * 
 * @author kanghongwei
 */

public class PatrolValidDaoImpl extends MultiDataSourceDaoImpl<AkaAuditUnit> implements PatrolValidDao {

	private static final Log log = LogFactory.getLog(PatrolValidDaoImpl.class);

	private PartitionStrategy strategy;

	public List<AkaAuditUnit> findValidUnitList(int tableIndex) {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append("       s.id, s.uid, m.title, m.description1, m.description2, ");
		sql.append("       m.targetUrl, m.showUrl, ");
		sql.append("       m.wireless_target_url, m.wireless_show_url ");
		sql.append("from ");

		sql.append("       beidou.cprounitmater" + tableIndex + " m, ");
		sql.append("       beidou.cprounitstate" + tableIndex + " s, ");
		sql.append("       beidoucap.useraccount u, ");
		sql.append("       beidou.cproplan p, ");
		sql.append("       beidou.cprogroup g ");
		sql.append("where ");
		sql.append("       m.id=s.id ");
		sql.append("       and s.uid=u.userid ");
		sql.append("       and s.pid=p.planid ");
		sql.append("       and s.gid=g.groupid ");
		sql.append("       and u.ustate=0 ");
		sql.append("       and u.ushifenstatid in (2,3) ");
		sql.append("       and p.planstate=0 ");
		sql.append("       and g.groupstate=0 ");
		sql.append("       and s.state=0 ");
		sql.append("       and m.wuliaotype in (1,5) ");
		sql.append("       and m.is_smart = 0 ");
		sql.append("       and " + MultiDataSourceSupport.geneateUseridStr("s.uid"));
		sql.append("       group by s.uid");

		return super.findBySql(new AkaAuditUnitRowMapping(), sql.toString(), new Object[0], new int[0]);
	}

	public List<Unit> findUnits(List<Integer> userIds, List<Long> unitIds) {
		if (CollectionUtils.isEmpty(unitIds)) {
			return new ArrayList<Unit>(0);
		}
		List<Unit> units = null;
		if (CollectionUtils.isEmpty(userIds) || unitIds.size() != userIds.size()) {
			log.error("userId list is empty or unitIds.size() != userIds.size()");
		} else {
			units = new ArrayList<Unit>(unitIds.size());
			for (int index = 0; index < unitIds.size(); index++) {
				Unit unit = this.findUnitById(userIds.get(index), unitIds.get(index));
				units.add(unit);
			}
		}
		return units;
	}

	public Unit findUnitById(int userId, Long unitId) {

		PartID partId = strategy.getPartitions(new PartKeyBDidImpl(userId));
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append("       s.id, s.gid, s.pid, g.groupname, p.planname, s.state, ");
		sql.append("       s.chaTime, s.helpstatus, s.audittime, m.wid, m.fwid, u.username ");
		sql.append("from ");

		sql.append("       beidou.cprounitstate" + partId.getId() + " s, ");
		sql.append("       beidou.cprounitmater" + partId.getId() + " m, ");
		sql.append("       beidou.cproplan p, ");
		sql.append("       beidou.cprogroup g, ");
		sql.append("       beidoucap.useraccount u ");
		sql.append("where ");
		sql.append("       s.id=m.id ");
		sql.append("       and s.uid=u.userid ");
		sql.append("       and  s.pid=p.planid ");
		sql.append("       and s.gid=g.groupid ");
		sql.append("       and s.id=" + unitId);
		sql.append("       and " + MultiDataSourceSupport.geneateUseridStr("s.uid"));

		List<Unit> result = super.findBySql(userId, new UnitRowMapping(), sql.toString(), new Object[0], new int[0]);
		if (CollectionUtils.isNotEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	/**
	 * 审核拒绝物料
	 * 设置shadow_mc_version_id=0
	 */
	public void updateUnit(int userId, Unit unit) {
		PartID partId = strategy.getPartitions(new PartKeyBDidImpl(userId));

        StringBuilder sql = new StringBuilder();
        sql.append("update beidou.cprounitstate" + partId.getId() + " s, beidou.cprounitmater" 
                + partId.getId() + " m set state=?, audittime=?, helpstatus=?, " 
                + "wid=?, fwid=?, fileSrc=?, shadow_mc_version_id=0 " + "where s.id=m.id and s.id=" + unit.getId());

		Object[] params = new Object[] { unit.getState(), unit.getAuditTime(), unit.getHelpstatus(), unit.getWid(), unit.getFwid(), unit.getFileSrc() };
		int[] argTypes = new int[] { Types.TINYINT, Types.TIMESTAMP, Types.INTEGER, Types.BIGINT, Types.BIGINT, Types.VARCHAR };

		super.updateBySql(userId, sql.toString(), params, argTypes);
	}

	public void addAuditing(int userId, UnitAuditing auditRea) {
		PartID partId = strategy.getPartitions(new PartKeyBDidImpl(userId));

		StringBuilder sql = new StringBuilder();
		sql.append("insert into beidou.unitauditing" + partId.getId() + " (unitid, refuseRea, userid) values (?,?,?);");

		Object[] params = new Object[] { auditRea.getId(), auditRea.getRefuseReason(), userId };
		int[] argTypes = new int[] { Types.BIGINT, Types.VARCHAR, Types.INTEGER };

		super.updateBySql(userId, sql.toString(), params, argTypes);
	}
	
    /**
     * Function: 删除已上线的版本
     * 
     * @author genglei01
     * @param userId userId
     * @param unitId unitId
     */
    public void deleteOnlineUnit(int userId, Long unitId) {
        String sql = "delete from beidou.online_unit where id = " + unitId;
        super.updateBySql(userId, sql, null, null);
    }

	public PartitionStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(PartitionStrategy stategy) {
		this.strategy = stategy;
	}

}
