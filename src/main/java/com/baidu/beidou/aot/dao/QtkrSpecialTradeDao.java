package com.baidu.beidou.aot.dao;

import java.util.List;

/**
 * 
 * @author kanghongwei
 * 
 *         refactor by kanghongwei since 2012-10-31
 */
public interface QtkrSpecialTradeDao {

	/**
	 * 获取计算优化建议时，需要特殊处理的用户二级行业
	 * 
	 * @return
	 */
	public List<Integer> findQtkrSpecialTradeId();
}
