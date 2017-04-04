package com.baidu.beidou.atleft.dao.impl;

import java.util.List;

import com.baidu.beidou.atleft.dao.AtLeftTradeDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class AtLeftTradeDaoImpl extends GenericDaoImpl implements AtLeftTradeDao{

	@Override
	public <E> List<E> getTradeInfo(GenericRowMapping<E> mappper,String sql, Object[] params) {
		return super.findBySql(mappper, sql, params);
	}

	@Override
	public int insertTrade(String sql, Object[] params,int[] types) {
		return super.updateBySql(sql, params,types);
	}

	@Override
	public int updateTrade(String sql, Object[] params,int[] types) {
		return super.updateBySql(sql, params, types);
	}

	@Override
	public int deleteTrade(String sql, Object[] params,int[] types) {
		return super.updateBySql(sql, params, types);
	}

}
