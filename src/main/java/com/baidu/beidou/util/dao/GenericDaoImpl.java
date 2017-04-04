package com.baidu.beidou.util.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.util.Assert;

import com.baidu.beidou.util.page.DataPage;

public class GenericDaoImpl {

	private Log log = LogFactory.getLog(GenericDaoImpl.class);

	private JdbcTemplate jdbcTemplate;

	/**
	 * @param jdbcTemplate
	 *            the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @SuppressWarnings("unchecked")
	protected <E> List<E> findBySql(GenericRowMapping<E> mappper, String sql,
			Object[] parameters, int[] argTypes) {
		log.debug(sql);
		return jdbcTemplate.query(sql, parameters, argTypes, mappper);
	}

	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> findBySql(String sql,
			Object[] parameters, int[] argTypes) {
		log.debug(sql);
		return jdbcTemplate.queryForList(sql, parameters, argTypes);
	}
	
	@SuppressWarnings("unchecked")
	protected <E> List<E> findBySql(GenericRowMapping<E> mappper, String sql,
			Object[] parameters) {
		log.debug(sql);
		return jdbcTemplate.query(sql, parameters,  mappper);
	}

	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> findBySql(String sql,
			Object[] parameters) {
		log.debug(sql);
		return jdbcTemplate.queryForList(sql, parameters);
	}
	
	
	protected int findIntBySql(String sql, Object[] parameters, int[] argTypes){
		return jdbcTemplate.queryForInt(sql, parameters, argTypes);
	}

	/**
	 * 提供分页查询功能
	 * 
	 * @param sql
	 * @param parameters
	 * @param argTypes
	 * @param currPage
	 * @param pageSize
	 * @return上午11:04:18
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findBySql(final String sql,
			final Object[] parameters, final int[] argTypes,
			final Integer currPage, final Integer pageSize) {
		log.debug(sql);
		if (currPage == null || pageSize == null) {
			return new ArrayList<Map<String, Object>>(0);
		}

		String limitSql = sql;

		int lastRowNum = currPage.intValue() * pageSize.intValue();
		int size = pageSize.intValue();
		limitSql = limitSql + " LIMIT " + lastRowNum + ", " + size;

		log.debug(limitSql);

		return jdbcTemplate.queryForList(limitSql, parameters, argTypes);
	}

	@SuppressWarnings("unchecked")
	protected <E> List<E> findBySql(GenericRowMapping<E> mappper, String sql,
			Object[] parameters, int[] argTypes, int curPage, int pageSize) {
		log.debug(sql);
		if (curPage < 0 && pageSize < 0) {
			return findBySql(mappper, sql, parameters, argTypes);
		}

		// oracle实现
		// long firstRowNum = currPage.longValue() * pageSize.longValue() + 1;
		// long endRowNum = firstRowNum + pageSize.longValue();
		// sql = "select * from (select rownum rn,t.* from (" + sql
		// + ") t) where rn>=" + firstRowNum + " and rn<" + endRowNum;

		// mysql实现
		long lastRowNum = ((long) curPage) * pageSize;
		long size = pageSize;
		sql = sql + " LIMIT " + lastRowNum + ", " + size;

		return jdbcTemplate.query(sql, parameters, argTypes, mappper);
	}
	
	private NativeJdbcExtractor getNativeJdbcExtractor() {
		return jdbcTemplate.getNativeJdbcExtractor();
	}
	
	/**
	 * 分页查询
	 * @param mapper
	 * @param sql
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <K> DataPage<K> queryPage(GenericRowMapping<K> mapper, String sql, int pageNo, int pageSize) throws DataAccessException {
		ResultSetExtractor rse = new RowMapperResultSetExtractor(mapper);
		Assert.notNull(rse, "ResultSetExtractor must not be null");

		sql = buildQueryPageSQL(sql, pageNo, pageSize);
		
		Connection con = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
		PreparedStatement ps = null;
		try {
			Connection conToUse = con;
			if (getNativeJdbcExtractor() != null) {
				conToUse = getNativeJdbcExtractor().getNativeConnection(con);
			}
			ps = conToUse.prepareStatement(sql);
			applyStatementSettings(ps);
			PreparedStatement psToUse = ps;
			if (getNativeJdbcExtractor() != null) {
				psToUse = getNativeJdbcExtractor().getNativePreparedStatement(ps);
			}
			ResultSet rs = psToUse.executeQuery();
			ResultSet rsToUse = rs;
			if (getNativeJdbcExtractor() != null) {
				rsToUse = getNativeJdbcExtractor().getNativeResultSet(rs);
			}
			
			List<K> record = (List<K>)rse.extractData(rsToUse);
			
			PreparedStatement psForTotal = conToUse.prepareStatement("select found_rows() as ct");
			if (getNativeJdbcExtractor() != null) {
				psToUse = getNativeJdbcExtractor().getNativePreparedStatement(psForTotal);
			}
			
			rs = psToUse.executeQuery();
			int totalCount = rs.getInt(1);
			
			if (!jdbcTemplate.isIgnoreWarnings() && ps.getWarnings() != null) {
				throw new SQLWarningException("Warning not ignored", ps.getWarnings());
			}
			
			DataPage<K> result = new DataPage<K>(record, totalCount,pageSize, pageNo);
			
			return result;
		}
		catch (SQLException ex) {
			JdbcUtils.closeStatement(ps);
			ps = null;
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
			con = null;
			throw jdbcTemplate.getExceptionTranslator().translate("CallableStatementCallback", sql, ex);
		}
		finally {
			JdbcUtils.closeStatement(ps);
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
		}
	}
	
	private String buildQueryPageSQL(String sql, int pageNo, int pageSize) {
        pageNo = pageNo > 0 ? pageNo : 1;
        int oBegin, oEnd;
        String regEx = "^\\s*select\\s+";
        Pattern pt = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher mc = pt.matcher(sql);
        sql = mc.replaceFirst("select SQL_CALC_FOUND_ROWS ");

        regEx = "(.*)\\s+limit\\s+(\\d+)\\s*,?\\s*(\\d*).*";
        pt = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        mc = pt.matcher(sql);
        if (mc.find()) {
            if (!mc.group(3).equals("")) {
                oBegin = Integer.parseInt(mc.group(2));
                oEnd = oBegin + Integer.parseInt(mc.group(3));
            } else {
                oBegin = 0;
                oEnd = Integer.parseInt(mc.group(2));
            }
            int begin = oBegin + (pageNo - 1) * pageSize > oEnd ? oEnd : oBegin + (pageNo - 1) * pageSize;
            int distinct = oEnd - (oBegin + pageNo * pageSize);
            int offset = distinct >= 0 ? pageSize : oEnd - begin;
            sql = mc.replaceAll("$1 limit " + begin + "," + offset);
        } else {
            sql = sql + " limit " + (pageNo - 1) * pageSize + "," + pageSize;
        }
        
        return sql;
	}
	
	protected void applyStatementSettings(Statement stmt) throws SQLException {
		int fetchSize = jdbcTemplate.getFetchSize();
		if (fetchSize > 0) {
			stmt.setFetchSize(fetchSize);
		}
		int maxRows = jdbcTemplate.getMaxRows();
		if (maxRows > 0) {
			stmt.setMaxRows(maxRows);
		}
		DataSourceUtils.applyTimeout(stmt, jdbcTemplate.getDataSource(), jdbcTemplate.getQueryTimeout());
	}

	protected void executeBySql(final String sql, final Object[] parameters) {
		log.debug(sql);
		jdbcTemplate.update(sql, parameters);
	}
	
	protected void executeBySql(final String sql, final Object[] parameters, final int[] argTypes) {
		log.debug(sql);
		jdbcTemplate.update(sql, parameters,argTypes);
	}
	protected int updateBySql(final String sql, final Object[] parameters) {
		log.debug(sql);
		return jdbcTemplate.update(sql, parameters);
	}
	
	protected int updateBySql(final String sql, final Object[] parameters, final int[] argTypes) {
		log.debug(sql);
		return jdbcTemplate.update(sql, parameters,argTypes);
	}
	

	@SuppressWarnings("unchecked")
	protected Long countBySql(String sql, Object[] parameters, int[] argTypes) {
		log.debug(sql);
		Long length = Long.valueOf(0);
		List list = jdbcTemplate.queryForList(sql, parameters, argTypes);
		if (list != null && list.size() > 0) {
			Map map = (Map) list.get(0);
			Iterator it = map.keySet().iterator();
			length = (Long) map.get(it.next());
		}
		return length;
	}

	public boolean ifTableExists(String tableName) {
		String sql = "select count(*) ct from information_schema.tables where table_name = '"
				+ tableName + "' and table_schema = 'beidoustat'";
		int res = jdbcTemplate.queryForInt(sql);
		if (res > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean ifTableExists(String tableName, String tableSchema) {
		String sql = "select count(*) ct from information_schema.tables where table_name = '"
				+ tableName + "' and table_schema = '" + tableSchema + "'";
		int res = jdbcTemplate.queryForInt(sql);
		if (res > 0) {
			return true;
		} else {
			return false;
		}
	}
}
