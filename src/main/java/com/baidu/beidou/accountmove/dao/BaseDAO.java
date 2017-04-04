package com.baidu.beidou.accountmove.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;


/**
 *  database dao
 * @author work
 *
 */
public class BaseDAO {
	
	private JdbcTemplate jdbcTemplate;
	
	/**
	 *  used to insert a record into a table
	 * @param sql sql
	 * @param params params
	 * @return result
	 */
	public String insertObject(final String sql, final Object[] params) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		int rowNum = this.jdbcTemplate.update(
				new PreparedStatementCreator() {
					@Override
					public PreparedStatement createPreparedStatement(
							java.sql.Connection connection) throws SQLException {
						PreparedStatement ps = connection
								.prepareStatement(sql);
						int index = 1;
						for (Object param : params) {
							ps.setObject(index++, param);
						}
						return ps;
					}
				}, keyHolder);
		if (rowNum > 0 && keyHolder.getKey() != null) {
			long keyValue = keyHolder.getKey().longValue();
			return String.valueOf(keyValue);
		} else {
			return null;
		}
	}
	
	/**
	 *  query info from db,with sql and param,
	 *  result will be in a list
	 * @param sql sql
	 * @param params params
	 * @return result
	 */
	public List<List<String>> queryObject(String sql, Object[] params) {
		return jdbcTemplate.query(sql, params, listMapping);
	}
	
	private RowMapper<List<String>> listMapping = new RowMapper<List<String>>() {

		public List<String> mapRow(ResultSet rs, int rowNum) throws SQLException {
			List<String> res = new ArrayList<String>();
			ResultSetMetaData meta = rs.getMetaData();
			for (int i = 1; i<=meta.getColumnCount(); i++) {
				res.add(rs.getString(i));
			}
			return res;
		}
	};

	/**
	 *  get primary key from sequence
	 * @param sequenceName sequenceName
	 * @param step step
	 * @return result
	 */
	public long generateKeys(String sequenceName, int step, String dbName) {

		//参数校验
		if (sequenceName == null || sequenceName.length() == 0 || step <= 0) {
			throw new RuntimeException("sequence not exist : " + sequenceName);
		}

		//构造SQL
		String querySQL = "select " + dbName + ".get_next_values (?, ?)";

		//执行操作
		Long value = jdbcTemplate.queryForLong(querySQL, new Object[]{sequenceName,step});
		if(value == null || value <= 0) {
			throw new RuntimeException("sequence not exist : " + sequenceName);
		}
		return value;
	}
	
	
	/**
	 *  batch insert
	 * @param sql sql 
	 * @param paramsList param list
	 * @return size
	 */
	public int batchInsertObject(String sql, final List<List<Object>> paramsList) {

		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			public void setValues(PreparedStatement stmt, int index)
					throws SQLException {
				int i = 1;
				List<Object> params = paramsList.get(index);
				for (Object o : params) {
					stmt.setObject(i++, o);
				}
			}
			public int getBatchSize() {
				return paramsList.size();
			}
		});

		return paramsList.size();
	}
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}
