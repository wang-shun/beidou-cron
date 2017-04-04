package com.baidu.beidou.shrink.dao.impl;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.shrink.dao.SimpleSQLDataSourceDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class SimpleSQLDataSourceDaoImpl extends GenericDaoImpl implements
		SimpleSQLDataSourceDao {

	@Override
	public List<Map<String, Object>> queryBySql(String sql, Object[] parameters) {
		return super.findBySql(sql, parameters);
	}

	@Override
	public <E> List<E> queryBySql(GenericRowMapping<E> mappper, String sql,
			Object[] parameters) {
		return super.findBySql(mappper,sql, parameters);
	}

	@Override
	public int saveBySql(String sql, Object[] parameters) {
		return super.updateBySql(sql, parameters);
	}
}
