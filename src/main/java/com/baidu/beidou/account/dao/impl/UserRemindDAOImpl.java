package com.baidu.beidou.account.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.baidu.beidou.account.bo.UserRemind;
import com.baidu.beidou.account.dao.UserRemindDAO;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class UserRemindDAOImpl extends GenericDaoImpl implements UserRemindDAO {

	/** 构造map的基础大小 2^15 */
	private int baseSize = Double.valueOf(Math.pow(2, 15)).intValue();

	private GenericRowMapping<UserRemind> rowMapper = new GenericRowMapping<UserRemind>() {

		public UserRemind mapRow(ResultSet rs, int rowNum) throws SQLException {

			UserRemind bo = new UserRemind();
			bo.setRemindId(rs.getInt(1));
			bo.setUserId(rs.getInt(2));
			bo.setRemindType(rs.getInt(3));
			bo.setEmail(rs.getString(4));
			bo.setMobile(rs.getString(5));
			return bo;
		}

	};

	public List<UserRemind> findRemindRecByUser(Integer userId) {
		String sql = "SELECT a.remindId, a.userid, a.remindtype, a.email, a.mobile " + "FROM beidouext.userremind a where a.userid=? order by a.remindId desc";
		List<UserRemind> result = null;
		result = super.findBySql(rowMapper, sql, new Object[] { userId }, new int[] { Types.INTEGER });

		return result;

	}

	public Map<Integer, UserRemind> findRemindRecByType(Integer type) {

		String sql = "SELECT a.remindid, a.userid, a.remindtype, a.email, a.mobile " + "FROM beidouext.userremind a where a.remindtype=? order by a.remindid desc";

		final Map<Integer, UserRemind> result = new HashMap<Integer, UserRemind>(baseSize);
		getJdbcTemplate().query(sql, new Object[] { type }, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				do {
					UserRemind bo = new UserRemind();
					bo.setRemindId(rs.getInt(1));
					bo.setUserId(rs.getInt(2));
					bo.setRemindType(rs.getInt(3));
					bo.setEmail(rs.getString(4));
					bo.setMobile(rs.getString(5));
					result.put(bo.getUserId(), bo);
				} while (rs.next());
			}

		});
		return result;

	}

	public List<UserRemind> findRemindRecByUserAndType(Integer userId, Integer type) {
		String sql = "SELECT a.remindId, a.userid, a.remindtype, a.email, a.mobile " + "FROM beidouext.userremind a where a.userid=? and a.remindtype=? order by a.remindId desc";
		List<UserRemind> result = null;
		result = super.findBySql(rowMapper, sql, new Object[] { userId, type }, new int[] { Types.INTEGER, Types.INTEGER });

		return result;

	}
}
