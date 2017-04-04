package com.baidu.beidou.accountmove.dao;

import java.util.List;

/**
 *  database access service
 *  provider database access manage
 * @author wangxiongjie
 *
 */
public interface DataAccessService {
	
	/**
	 * query info from database, use userId to get the right shard to query
	 * @param sql sql
	 * @param params params array
	 * @param userId userId
	 * @return result
	 */
	List<List<String>> queryInfo(String sql, Object[] params, int userId);
	
	/**
	 * write info into database, use userId to get the right shard
	 * @param sql sql
	 * @param userId userid
	 * @return result
	 */
	String updateInfo(String sql, Object[] params, int userId);
	
	/**
	 * get primary key from sequence
	 * @param sequenceName sequenceName
	 * @param step step
	 * @return result
	 */
	long generateKeys(int userId, String sequenceName, int step);
	
	/**
	 * 
	 * @param userId
	 * @param sequenceName
	 * @param step
	 * @return array
	 */
	Long[] getNextKeywordIdBatch(int userId, String sequenceName, int step);
	
	/**
	 *  update info in batch;
	 * @param sql sql
	 * @param paramsList param list
	 * @param userId userid
	 * @return size
	 */
	int updateInfoBatch(String sql, List<List<Object>> paramsList, int userId);
}
