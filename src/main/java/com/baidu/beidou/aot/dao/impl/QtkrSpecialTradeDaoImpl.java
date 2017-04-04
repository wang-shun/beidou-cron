package com.baidu.beidou.aot.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.aot.dao.QtkrSpecialTradeDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * 
 * @author kanghongwei
 * 
 *         refactor by kanghongwei since 2012-10-31
 */
public class QtkrSpecialTradeDaoImpl extends GenericDaoImpl implements QtkrSpecialTradeDao {

	public List<Integer> findQtkrSpecialTradeId() {

		String sql = "select secondtradeid from aot.qtkrspecialtrade";
		List<Map<String, Object>> result = super.findBySql(sql, new Object[] {}, new int[] {});

		List<Integer> secondTradeIdList = new ArrayList<Integer>();
		for (Map<String, Object> row : result) {
			secondTradeIdList.add((Integer) (row.get("secondtradeid")));
		}

		return secondTradeIdList;
	}
}
