package com.baidu.beidou.atleft.dao;

import java.util.List;

import com.baidu.beidou.util.dao.GenericRowMapping;

public interface AtLeftTradeDao {
	<E> List<E> getTradeInfo(GenericRowMapping<E> mappper,String sql,Object[] params);
	
	int insertTrade(String sql,Object[] params,int[] types);
	
	int updateTrade(String sql,Object[] params,int[] types);
	
	int deleteTrade(String sql,Object[] params,int[] types);
}
