package com.baidu.beidou.shrink.dao;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 简单执行sql的dao
 * @author hexiufeng
 *
 */
public interface SimpleSQLDataSourceDao {
	List<Map<String, Object>> queryBySql(String sql, Object[] parameters);
	<E> List<E> queryBySql(GenericRowMapping<E> mappper, String sql, Object[] parameters);
	
	int saveBySql(String sql, Object[] parameters);
}
