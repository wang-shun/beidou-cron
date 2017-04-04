/**
 * 2010-3-16 下午04:38:39
 */
package com.baidu.beidou.auditmanager.dao.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import com.baidu.beidou.auditmanager.dao.AuditHistoryDao;
import com.baidu.beidou.auditmanager.vo.AuditHistoryView;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

/**
 * @author zengyunfeng
 * @version 1.0.43
 */
public class AuditHistoryDaoImpl extends MultiDataSourceDaoImpl<Integer> implements AuditHistoryDao {
	private static final Log LOG = LogFactory.getLog(AuditHistoryDaoImpl.class);

	private static final String REFUSE_ID_SPLIT = " ";

	public boolean findAuditHistoryAndOutputMonitorFile(final BufferedWriter output, final Set<String> monitorReason, final Calendar startTime) {
		if (startTime == null || monitorReason == null || monitorReason.isEmpty() || output == null) {
			return false;
		}

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT userid, refuseReason from beidou.auditcprounithistory WHERE auditresult = 1 AND optime > ? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

		final boolean[] canWrite = new boolean[] { true };
		super.findBySql(new GenericRowMapping<Integer>() {

			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				if (!canWrite[0]) {
					return null;
				}
				int userId = rs.getInt(1);
				String refuseReason = rs.getString(2);
				if (refuseReason == null || refuseReason.length() == 0) {
					return null;
				}
				String[] fields = refuseReason.split(REFUSE_ID_SPLIT);
				try {
					for (String field : fields) {
						field = field.trim();
						if (monitorReason.contains(field)) {
							output.write(String.valueOf(userId));
							output.write('\t');
							output.write(field);
							output.write('\n');
						}
					}
				} catch (IOException e) {
					canWrite[0] = false;
					LogUtils.fatal(LOG, e.getMessage(), e);
				}
				return null;
			}
		}, sql.toString(), new Object[] { startTime.getTime() }, new int[] { Types.TIMESTAMP });

		return canWrite[0];
	}

	/**
	 * findNotSyncAuditHistory: 获取未同步到ubmc的审核历史
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<AuditHistoryView> findNotSyncAuditHistory(int maxMaterNum) {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from beidou.auditcprounithistory where ubmcsyncflag=0");
		this.appendUserIdRouting(sql, "userid");
		sql.append(" order by optime desc limit ?");
		
		return super.findBySql(new AuditHistoryViewRowMapping(), sql.toString(), 
				new Object[] { maxMaterNum }, new int[] { Types.INTEGER });
	}
	
	/**
	 * updateAuditHistory: 更新审核历史mcId、mcVersionId以及已同步字段
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateAuditHistory(Integer id, Long mcId, Integer mcVersionId, Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("update beidou.auditcprounithistory set ubmcsyncflag=1, mcId=?, mcVersionId=? where id=?");
		this.appendUserIdRouting(sql, "userid");

		Object[] params = new Object[] { mcId, mcVersionId, id };
		int[] paramTypes = new int[]{ Types.BIGINT, Types.INTEGER, Types.INTEGER};

		super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	
	private class AuditHistoryViewRowMapping implements GenericRowMapping<AuditHistoryView> {
		public AuditHistoryView mapRow(ResultSet rs, int rowNum) throws SQLException, DataAccessException {
			AuditHistoryView auditHistory = new AuditHistoryView();
			auditHistory.setId(rs.getInt("id"));
			auditHistory.setUnitId(rs.getLong("unitid"));
			auditHistory.setUserId(rs.getInt("userid"));
			auditHistory.setWid(rs.getLong("wid"));
			auditHistory.setWuliaoType(rs.getInt("wuliaoType"));
			
			auditHistory.setTitle(rs.getString("title"));
			auditHistory.setDescription1(rs.getString("description1"));
			auditHistory.setDescription2(rs.getString("description2"));
			auditHistory.setShowUrl(rs.getString("showUrl"));
			auditHistory.setTargetUrl(rs.getString("targetUrl"));
			auditHistory.setWirelessShowUrl(rs.getString("wireless_show_url"));
			auditHistory.setWirelessTargetUrl(rs.getString("wireless_target_url"));
			
			auditHistory.setFileSrc(rs.getString("fileSrc"));
			auditHistory.setHeight(rs.getInt("height"));
			auditHistory.setWidth(rs.getInt("width"));
			
			auditHistory.setUbmcsyncflag(rs.getInt("ubmcsyncflag"));
			auditHistory.setMcId(rs.getLong("mcId"));
			auditHistory.setMcVersionId(rs.getInt("mcVersionId"));
			
			return auditHistory;
		}
	}
}
