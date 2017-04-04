package com.baidu.beidou.auditmanager.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;

import com.baidu.beidou.auditmanager.constant.AuditConstant;
import com.baidu.beidou.auditmanager.dao.UrlCheckHistoryDao;
import com.baidu.beidou.auditmanager.vo.Reason;
import com.baidu.beidou.auditmanager.vo.UrlUnit;
import com.baidu.beidou.auditmanager.vo.UrlUnitForMail;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class UrlCheckHistoryDaoImpl extends GenericDaoImpl implements UrlCheckHistoryDao {

	private static final Log log = LogFactory.getLog(UrlCheckHistoryDaoImpl.class);

	private static int CNT_PER_INSERT_SQL = 100;

	public void insertUrlCheckHistory(List<UrlUnit> urlUnitList, int type) {
		int toIndex = 0;
		int length = urlUnitList.size();

		for (int index = 0; index < length; index = toIndex) {
			toIndex = index + CNT_PER_INSERT_SQL;
			if (toIndex > length) {
				toIndex = length;
			}
			_insertUrlCheckHistory(urlUnitList.subList(index, toIndex), type);
		}
	}

	private void _insertUrlCheckHistory(final List<UrlUnit> urlUnitList, final int type) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into history.urlcheckhistory (unitid,groupid,planid,userid," + "groupname,planname,username,state,targetUrl,subTime,chaTime,audittime," + "type,auditresult,reasonid) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		getJdbcTemplate().execute(sb.toString(), new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				for (int i = 0; i < urlUnitList.size(); i++) {
					UrlUnit urlUnit = urlUnitList.get(i);
					if (urlUnit.getRefused() == 1) {
						ps.setLong(1, urlUnit.getId());
						ps.setInt(2, urlUnit.getGroupId());
						ps.setInt(3, urlUnit.getPlanId());
						ps.setInt(4, urlUnit.getUserId());
						ps.setString(5, urlUnit.getGroupName());
						ps.setString(6, urlUnit.getPlanName());
						ps.setString(7, urlUnit.getUserName());
						ps.setInt(8, urlUnit.getState());
						ps.setString(9, urlUnit.getTargetUrl());
						ps.setTimestamp(10, new java.sql.Timestamp(urlUnit.getSubTime().getTime()));
						ps.setTimestamp(11, new java.sql.Timestamp(urlUnit.getChaTime().getTime()));
						ps.setTimestamp(12, new java.sql.Timestamp(urlUnit.getAuditTime().getTime()));
						ps.setInt(13, type);
						ps.setInt(14, AuditConstant.AUDIT_REFUSE);
						ps.setInt(15, urlUnit.getReasonId());

						ps.addBatch();
					}
				}
				ps.executeBatch();

				return null;
			}
		});

		log.info("batch add URL check history:" + urlUnitList.size() + "个");
	}

	public List<UrlUnitForMail> getUrlCheckHistory(Date startTime, Date endTime, Integer type) {
		StringBuilder builder = new StringBuilder();
		builder.append("select groupid,userid,groupname,planname,username,reasonid,count(*) as count").append(" from history.urlcheckhistory ").append(" where type=? and auditresult=? and audittime>=? and audittime<=?").append(" group by groupid, reasonid;");

		Object[] args = new Object[] { type, AuditConstant.AUDIT_REFUSE, startTime, endTime };
		int[] argTypes = new int[] { Types.TINYINT, Types.TINYINT, Types.TIMESTAMP, Types.TIMESTAMP };

		return super.findBySql(new UrlCheckHistoryRowMapper(), builder.toString(), args, argTypes);
	}

	private class UrlCheckHistoryRowMapper implements GenericRowMapping<UrlUnitForMail> {
		public UrlUnitForMail mapRow(ResultSet rs, int rowNum) throws SQLException {
			UrlUnitForMail urlUnit = new UrlUnitForMail();

			urlUnit.setGroupId(rs.getInt("groupid"));
			urlUnit.setUserId(rs.getInt("userid"));
			urlUnit.setGroupName(rs.getString("groupname"));
			urlUnit.setPlanName(rs.getString("planname"));
			urlUnit.setUserName(rs.getString("username"));

			int reasonId = rs.getInt("reasonid");
			Reason reason = AuditConstant.reasonMap.get(reasonId);
			if (reason != null) {
				urlUnit.setReason(reason.getClient());
			} else {
				log.error("can not find refuse reason for " + reasonId);
				urlUnit.setReason("");
			}

			urlUnit.setCount(rs.getInt("count"));

			return urlUnit;
		}
	}

	public int getCNT_PER_INSERT_SQL() {
		return CNT_PER_INSERT_SQL;
	}

	public void setCNT_PER_INSERT_SQL(int cnt_per_insert_sql) {
		CNT_PER_INSERT_SQL = cnt_per_insert_sql;
	}
}
