package com.baidu.beidou.util.dao;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.util.Assert;

import com.baidu.beidou.auditmanager.service.ResultSetCallBack;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.page.DataPage;
import com.baidu.beidou.util.string.StringUtil;

public class MultiDataSourceDaoImpl<T> {
	
	public static final int shardingNum = 8;

	// “创意单库分表数”
	public static final int TAB_UNIT_SLICE = 8;

	private MultiDataSourceSupport dataSourceSupport;

	private String dbName = MultiDataSourceSupport.ADDB;

	private JdbcTemplate jdbcTemplate;

	/**
	 * @param jdbcTemplate
	 *            the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private Class<T> persistentClass;

	@SuppressWarnings("unchecked")
	public MultiDataSourceDaoImpl() {
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public Class<T> getPersistentClass() {
		return persistentClass;
	}

	@SuppressWarnings("unchecked")
	protected <E> List<E> findBySql(GenericRowMapping<E> mappper, String sql, Object[] parameters, int[] argTypes) {
		if (sql == null) {
			return null;
		}
		List<E> res = new ArrayList<E>();
		for (int i = 0; i < shardingNum; i++) {
			String oldDBKey = dataSourceSupport.setDataSource(dbName, false, i);
			String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, i);
			List<E> subRes = jdbcTemplate.query(shardingSql, parameters, argTypes, mappper);
			if (subRes != null) {
				res.addAll(subRes);
			}
			dataSourceSupport.setDataSource(dbName, oldDBKey);
		}
		return res;
	}

	/**
	 * 在固定的sharding上做查询操作，这个sharding是某个确定的分片，比如0到7之间的一个数
	 * 
	 * @param sharding
	 * @param sql
	 * @param parameters
	 * @param argTypes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> findBySqlOnSharding(int sharding, String sql, Object[] parameters, int[] argTypes) {
		if (sql == null || sharding < 0 || sharding >= shardingNum) {
			return null;
		}
		String oldDBKey = dataSourceSupport.setDataSource(dbName, false, sharding);
		String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, sharding);
		List<Map<String, Object>> res = jdbcTemplate.queryForList(shardingSql, parameters, argTypes);
		dataSourceSupport.setDataSource(dbName, oldDBKey);
		return res;
	}
	
	
    @SuppressWarnings("unchecked")
    protected <E> List<E> findBySqlOnSharding(int sharding, GenericRowMapping<E> mapper, String sql,
            Object[] parameters, int[] argTypes) {
        if (sql == null || sharding < 0 || sharding >= shardingNum) {
            return null;
        }

        String oldDBKey = dataSourceSupport.setDataSource(dbName, false, sharding);
        String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, sharding);
        List<E> res = jdbcTemplate.query(shardingSql, parameters, argTypes, mapper);
        dataSourceSupport.setDataSource(dbName, oldDBKey);
        return res;
    }

	/**
	 * 在固定的sharding上做查询操作，这个sharding是某个确定的分片，比如0到7之间的一个数
	 * 
	 * @param sharding
	 * @param sql
	 * @param parameters
	 * @param argTypes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <E> List<E> findBySqlOnSharding(int sharding, GenericRowMapping<E> mappper, String sql, Object[] parameters, int[] argTypes, Comparator<E> comparator, int page, int pageSize) {
		if (sql == null || sharding < 0 || sharding >= shardingNum) {
			return null;
		}

		long startRowNum = ((long) page) * pageSize;
		long size = pageSize;
		sql = sql + " LIMIT " + startRowNum + ", " + size;

		String oldDBKey = dataSourceSupport.setDataSource(dbName, false, sharding);
		String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, sharding);
		List<E> res = jdbcTemplate.query(shardingSql, parameters, argTypes, mappper);
		dataSourceSupport.setDataSource(dbName, oldDBKey);
		return res;
	}

	@SuppressWarnings("unchecked")
	protected <E> List<E> findBySql(Integer userid, GenericRowMapping<E> mappper, String sql, Object[] parameters, int[] argTypes) {
		if (sql == null) {
			return null;
		}
		String oldDBKey = dataSourceSupport.setDataSourceByUserid(dbName, false, userid);
		String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, MultiDataSourceSupport.calculateDatabaseNo(userid));
		List<E> res = jdbcTemplate.query(shardingSql, parameters, argTypes, mappper);
		dataSourceSupport.setDataSource(dbName, oldDBKey);
		return res;
	}
	
	protected <E> List<E> findBySqlAndDealByResultCallBack(Integer userid, GenericRowMapping<E> mappper, String sql, Object[] parameters, int[] argTypes,
			ResultSetCallBack<E> callBack) {
		List<E> res = findBySql(userid,mappper,sql,parameters,argTypes);
		callBack.dealWithResultSet(res);
		return res;
	}

	protected <E> List<E> findBySqlWithOrder(GenericRowMapping<E> mappper, String sql, Object[] parameters, int[] argTypes, Comparator<E> comparator) {
		if (sql == null) {
			return null;
		}
		List<E> res = this.findBySql(mappper, sql, parameters, argTypes);

		if (res != null && res.size() > 0) {
			Collections.sort(res, comparator);
		}
		return res;
	}

	protected <E> List<E> findBySqlGetPage(GenericRowMapping<E> mappper, String sql, Object[] parameters, int[] argTypes, Comparator<E> comparator, int page, int pageSize) {
		if (sql == null) {
			return null;
		}
		if (page < 0 || pageSize <= 0) {
			return null;
		}
		List<E> res = this.findBySqlWithOrder(mappper, sql, parameters, argTypes, comparator);
		
		//本页需要从startindex获取数据，并取pageSize条数据
		int startindex = page * pageSize;
		if(res == null || res.size() == 0){
			return new ArrayList<E>(0);
		}else if(startindex >= res.size()){
			return new ArrayList<E>(0);
		}else{
			int endindex = (startindex + pageSize) <= res.size() ? (startindex + pageSize) : res.size();
			return res.subList(startindex, endindex);
		}
	}
	
	/**
	 * 分页查询
	 * 
	 * @param mapper
	 * @param sql
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <K> DataPage<K> queryPage(GenericRowMapping<K> mapper, int userId, String sql, int pageNo, int pageSize) throws DataAccessException {
		ResultSetExtractor rse = new RowMapperResultSetExtractor(mapper);
		Assert.notNull(rse, "ResultSetExtractor must not be null");

		sql = buildQueryPageSQL(sql, pageNo, pageSize);
		String oldDBKey = dataSourceSupport.setDataSourceByUserid(dbName, false, userId);
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
				psForTotal = getNativeJdbcExtractor().getNativePreparedStatement(psForTotal);
			}
			
			rs = psForTotal.executeQuery();
			rsToUse = rs;
			if (getNativeJdbcExtractor() != null) {
				rsToUse = getNativeJdbcExtractor().getNativeResultSet(rs);
			}
			int totalCount = record == null ? 0 : record.size();
			if (rsToUse.next()) {
				totalCount = rsToUse.getInt(1);
			}
			
			if (!jdbcTemplate.isIgnoreWarnings() && ps.getWarnings() != null) {
				throw new SQLWarningException("Warning not ignored", ps.getWarnings());
			}
			
			DataPage<K> result = new DataPage<K>(record, totalCount,pageSize, pageNo);
			
			return result;
		} catch (SQLException ex) {
			JdbcUtils.closeStatement(ps);
			ps = null;
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
			con = null;
			throw jdbcTemplate.getExceptionTranslator().translate("CallableStatementCallback", sql, ex);
		} finally {
			JdbcUtils.closeStatement(ps);
			DataSourceUtils.releaseConnection(con, jdbcTemplate.getDataSource());
			dataSourceSupport.setDataSource(dbName, oldDBKey);
		}
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
	
	private NativeJdbcExtractor getNativeJdbcExtractor() {
		return jdbcTemplate.getNativeJdbcExtractor();
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

	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> findBySql(String sql, Object[] parameters, int[] argTypes) {
		if (sql == null) {
			return null;
		}
		List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < shardingNum; i++) {
			String oldDBKey = dataSourceSupport.setDataSource(dbName, false, i);
			String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, i);
			List<Map<String, Object>> subRes = jdbcTemplate.queryForList(shardingSql, parameters, argTypes);
			if (subRes != null) {
				res.addAll(subRes);
			}
			dataSourceSupport.setDataSource(dbName, oldDBKey);
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> findBySql(Integer userId, String sql, Object[] parameters, int[] argTypes) {
		if (sql == null) {
			return null;
		}
		String oldDBKey = dataSourceSupport.setDataSourceByUserid(dbName, false, userId);
		String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, MultiDataSourceSupport.calculateDatabaseNo(userId));
		List<Map<String, Object>> res = jdbcTemplate.queryForList(shardingSql, parameters, argTypes);
		dataSourceSupport.setDataSource(dbName, oldDBKey);
		return res;
	}

	@SuppressWarnings("rawtypes")
	protected Long countBySql(String sql, Object[] parameters, int[] argTypes) {
		if (sql == null) {
			return null;
		}
		Long length = Long.valueOf(0);

		for (int i = 0; i < shardingNum; i++) {
			String oldDBKey = dataSourceSupport.setDataSource(dbName, false, i);
			String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, i);

			List list = jdbcTemplate.queryForList(shardingSql, parameters, argTypes);
			if (list != null && list.size() > 0) {
				Map map = (Map) list.get(0);
				Iterator it = map.keySet().iterator();
				length += (Long) map.get(it.next());
			}
			dataSourceSupport.setDataSource(dbName, oldDBKey);
		}
		return length;
	}

	/**
	 * 此方法在每个分片上执行update,但是不支持事务，需要事务的必须自己到facade上做循环多库
	 * 
	 * @param sql
	 * @return
	 */
	protected int updateBySql(String sql) {
		if (sql == null) {
			return 0;
		}
		int res = 0;
		for (int i = 0; i < shardingNum; i++) {
			String oldDBKey = dataSourceSupport.setDataSource(dbName, true, i);
			String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, i);
			int subRes = jdbcTemplate.update(shardingSql);
			res += subRes;
			dataSourceSupport.setDataSource(dbName, oldDBKey);
		}
		return res;
	}

	protected int updateBySql(String sql, Object[] parameters, int[] argTypes) {
		if (sql == null) {
			return 0;
		}
		int res = 0;
		for (int i = 0; i < shardingNum; i++) {
			String oldDBKey = dataSourceSupport.setDataSource(dbName, true, i);
			String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, i);
			int subRes = jdbcTemplate.update(shardingSql, parameters, argTypes);
			res += subRes;
			dataSourceSupport.setDataSource(dbName, oldDBKey);
		}
		return res;
	}

	/**
	 * 此方法可以根据指定的userid在固定的db sharding上做update
	 * 
	 * @param userId
	 * @param sql
	 * @param parameters
	 * @return
	 */
	protected int updateBySql(Integer userId, String sql, Object[] parameters, int[] argTypes) {
		if (sql == null) {
			return 0;
		}
		String oldDBKey = dataSourceSupport.setDataSourceByUserid(dbName, true, userId);
		String shardingSql = MultiDataSourceSupport.explainUseridStr(sql, MultiDataSourceSupport.calculateDatabaseNo(userId));
		int res = jdbcTemplate.update(shardingSql, parameters, argTypes);
		dataSourceSupport.setDataSource(dbName, oldDBKey);
		return res;
	}

	public void appendUserIdRouting(StringBuilder sql, String userIdField) {
		sql.append(" and ").append(MultiDataSourceSupport.geneateUseridStr(userIdField));
	}

	public MultiDataSourceSupport getDataSourceSupport() {
		return dataSourceSupport;
	}

	public void setDataSourceSupport(MultiDataSourceSupport dataSourceSupport) {
		this.dataSourceSupport = dataSourceSupport;
	}

}
