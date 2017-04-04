/**
 * 
 */
package com.baidu.beidou.cache.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.JdbcUtils;

import com.baidu.beidou.cache.dao.BeidouDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * @author wangqiang04
 * 
 */
public class BeidouDaoImpl extends GenericDaoImpl implements BeidouDao {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> findAllUserids() {
		final String sql = "select userid from beidoucap.useraccount";
		return (List<Integer>) getJdbcTemplate().execute(sql, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				List<Integer> list = new ArrayList<Integer>();

				ResultSet rs = null;
				try {
					rs = ps.executeQuery(sql);
					while (rs != null && rs.next()) {
						list.add(rs.getInt("userid"));
					}
				} finally {
					JdbcUtils.closeResultSet(rs);
				}
				return list;
			}
		});
	}

}
