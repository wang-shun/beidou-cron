package com.baidu.beidou.stat.util.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.baidu.beidou.stat.constant.SysProp;



/*
 * 数据库短连接的基类
 * @author jaq
 * @version 1.0.0
 */

public class BaseDAOSupport {
	private Log log = LogFactory.getLog(this.getClass());
	private JdbcTemplate jdbcTemplate;
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	/**
	 * 获得满足条件的所有记录个数
	 * @param sql select sql语句
	 * @param parameters 参数
	 * @return 由数据MAP组成的LIST
	 */
    public List findByCondition(final String sql, final Object[] parameters) {
    	return jdbcTemplate.queryForList(sql, parameters);
    }
    /**
     * 获得满足条件的记录个数
     * @param sql count sql语句
     * @param parameters
     * @return 满足条件的记录个数
     */
    public Long countByCondition(final String sql, final Object[] parameters){
    	return jdbcTemplate.queryForLong(sql, parameters);
    }
    /**
     * 分页获得返回结果
     * @param sql 执行select sql语句
     * @param parameters 参数
     * @param currPage 当前页码
     * @param pageSize 每页个数
     * @return 有数据MAP组成的LIST
     */
    public List findByCondition(final String sql, final Object[] parameters,
            final Integer currPage, final Integer pageSize){    	
    	
    	if (currPage == null || pageSize == null) {
            return SysProp.ZERO_LIST;
        }
    	
    	String limitSql = sql;
    	
    	int lastRowNum = currPage.intValue() * pageSize.intValue();
        int size = pageSize.intValue();
        limitSql = limitSql + " LIMIT " + lastRowNum + ", " + size;
        
        log.debug(limitSql);
        
    	return jdbcTemplate.queryForList(limitSql, parameters);
    }
    /**
     * 执行DDL操作
     * @param sql DDL sql语句
     * @param parameters 输入参数
     * @return 更新的记录条数
     */
    public int updateByCondition(String sql, Object[] parameters) {
    	return jdbcTemplate.update(sql, parameters);
    }
    public void excuteSql(String sql)throws Exception{
    	try{
    		jdbcTemplate.execute(sql);
    		log.info("sql execution success");
    	}catch(Exception e){
    		log.error("sql execution fail");
    		throw e;
    	}
    }
    public boolean ifTableExists(String tableName){
    	String sql = "select count(*) ct from information_schema.tables where table_name = '"+tableName+"'";
    	int res = jdbcTemplate.queryForInt(sql);
    	if(res>0){
    		return true;
    	}else{
    		return false;
    	}
    }
    public int queryForInt(String sql,Object[] parameters){
    	return jdbcTemplate.queryForInt(sql, parameters);
    }
    public void truncateTable(String table){
    	String sql = "TRUNCATE table "+table;
    	jdbcTemplate.execute(sql);
    }
}
