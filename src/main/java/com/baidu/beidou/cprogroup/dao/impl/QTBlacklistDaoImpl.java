package com.baidu.beidou.cprogroup.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.cprogroup.dao.QTBlacklistDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class QTBlacklistDaoImpl extends GenericDaoImpl implements QTBlacklistDao {

	public List<Long> findBlackListByUser(Integer userId) {
		String sql = "select wordid from beidoureport.qtuserblacklist where userid=?";
		return super.findBySql(new GenericRowMapping<Long>() {
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong(1);
			}
		}, sql, new Object[] { userId }, new int[] { Types.INTEGER });
	}

	public Map<Long, Integer> findQTBlackList() {
		String sql = "select wordid, tag from beidoureport.qtblacklist";
		final Map<Long, Integer> result = new HashMap<Long, Integer>();
		super.findBySql(new GenericRowMapping<Object>() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				result.put(rs.getLong(1), rs.getInt(2));
				return null;
			}
		}, sql, new Object[] {}, new int[] {});
		return result;
	}

}