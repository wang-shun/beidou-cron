package com.baidu.beidou.auditmanager.dao.impl;

import java.sql.Types;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.auditmanager.dao.UrlJumpCheckDao;
import com.baidu.beidou.auditmanager.vo.UnitAuditing;
import com.baidu.beidou.auditmanager.vo.UrlCheckUnit;
import com.baidu.beidou.auditmanager.vo.UrlUnit;
import com.baidu.beidou.cprogroup.dao.rowmap.UrlCheckUnitRowMapping;
import com.baidu.beidou.cprogroup.dao.rowmap.UrlUnitRowMapping;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;
import com.baidu.beidou.util.partition.PartID;
import com.baidu.beidou.util.partition.impl.PartKeyBDidImpl;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;

/**
 * ClassName: UrlJumpCheckDaoImpl Function: URL跳转校验DAO
 * 
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version
 * @date 2011-10-20
 * @see
 */
public class UrlJumpCheckDaoImpl extends MultiDataSourceDaoImpl<UrlUnit> implements UrlJumpCheckDao {

	private PartitionStrategy strategy;

	public UrlUnit findUrlUnitById(int userId, Long unitId) {
		if (unitId == null) {
			return null;
		}

		PartID partId = strategy.getPartitions(new PartKeyBDidImpl(userId));

		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append("	   s.id, s.gid, s.pid, s.uid, u.id, g.groupname, p.planname, ");
		sql.append("       u.username, s.state, s.subTime, s.chaTime, s.helpstatus, ");
		sql.append("       s.audittime, m.wid, m.fwid, m.targetUrl, m.fileSrc ,m.wireless_target_url ");
		sql.append("from ");
		sql.append("       beidou.cprounitstate" + partId.getId() + " s, ");
		sql.append("       beidou.cprounitmater" + partId.getId() + " m, ");
		sql.append("       beidou.cproplan p, ");
		sql.append("       beidou.cprogroup g, ");
		sql.append("       beidoucap.useraccount u ");
		sql.append("where ");
		sql.append("       s.id=m.id ");
		sql.append("       and s.uid=u.userid ");
		sql.append("	   and  s.pid=p.planid ");
		sql.append("       and s.gid=g.groupid ");
		sql.append("       and s.id=" + unitId);
		sql.append("       and " + MultiDataSourceSupport.geneateUseridStr("s.uid"));

		List<UrlUnit> result = super.findBySql(userId, new UrlUnitRowMapping(), sql.toString(), new Object[0], new int[0]);
		if (CollectionUtils.isNotEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	public List<UrlCheckUnit> findValidUrlList(int tableIndex) {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append("       s.id, s.uid, u.id, m.targetUrl ,m.wireless_target_url ");
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
		sql.append("       and m.is_smart=").append(CproUnitConstant.IS_SMART_FALSE);
		sql.append("       and " + MultiDataSourceSupport.geneateUseridStr("s.uid"));

		return super.findBySql(new UrlCheckUnitRowMapping(), sql.toString(), new Object[0], new int[0]);
	}

	/**
	 * 审核拒绝物料
	 * 设置shadow_mc_version_id=0
	 */
	public void updateUrlUnit(int userId, UrlUnit urlUnit) {
		PartID partId = strategy.getPartitions(new PartKeyBDidImpl(userId));

		StringBuilder sql = new StringBuilder();
        sql.append("update beidou.cprounitstate" + partId.getId() + " s, beidou.cprounitmater" + partId.getId()
                + " m set state=?, audittime=?, helpstatus=?, " + "wid=?, fwid=?, fileSrc=?, shadow_mc_version_id=0 "
                + "where s.id=m.id and s.id=" + urlUnit.getId());

		Object[] params = new Object[] { urlUnit.getState(), urlUnit.getAuditTime(), urlUnit.getHelpstatus(), urlUnit.getWid(), urlUnit.getFwid(), urlUnit.getFileSrc() };
		int[] argTypes = new int[] { Types.TINYINT, Types.TIMESTAMP, Types.INTEGER, Types.BIGINT, Types.BIGINT, Types.VARCHAR };

		super.updateBySql(userId, sql.toString(), params, argTypes);
	}

	public void addAuditing(int userId, UnitAuditing auditRea) {
		PartID partId = strategy.getPartitions(new PartKeyBDidImpl(userId));

		StringBuilder sql = new StringBuilder();
		sql.append("insert into beidou.unitauditing" + partId.getId() + " (unitid, refuseRea,userid) values (?,?,?);");

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

	public void setStrategy(PartitionStrategy strategy) {
		this.strategy = strategy;
	}

}
