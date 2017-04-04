package com.baidu.beidou.accountmove.dao.impl;

import java.util.List;

import com.baidu.beidou.accountmove.dao.BaseDAO;
import com.baidu.beidou.accountmove.dao.DataAccessService;

/**
 * DataAccessService Impl
 * @author work
 *
 */
public class DataAccessServiceImpl implements DataAccessService {
	
	private List<BaseDAO> baseDAOList;
	
	@Override
	public List<List<String>> queryInfo(String sql, Object[] params, int userId) {
		BaseDAO dao = getQueryDAO(userId);
		return dao.queryObject(sql, params);
	}

	@Override
	public String updateInfo(String sql, Object[] params, int userId) {
		BaseDAO dao = getQueryDAO(userId);
		return dao.insertObject(sql, params);
	}

	@Override
	public int updateInfoBatch(String sql, List<List<Object>> paramsList, int userId) {
		BaseDAO dao = getQueryDAO(userId);
//		return dao.insertObject(sql, params);
		return dao.batchInsertObject(sql, paramsList);
	}
	
	private BaseDAO getQueryDAO (int userid) {
		if (shardingNum == 1) {
			return baseDAOList.get(0);
		}
		int useridcode = (int)((userid >>> tableShardingLength) & (shardingNum - 1));
		return baseDAOList.get(useridcode);
	}
	
	public List<BaseDAO> getBaseDAOList() {
		return baseDAOList;
	}

	public void setBaseDAOList(List<BaseDAO> baseDAOList) {
		this.baseDAOList = baseDAOList;
	}

	public int getShardingNum() {
		return shardingNum;
	}

	public void setShardingNum(int shardingNum) {
		this.shardingNum = shardingNum;
	}
	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	private final int tableShardingLength = 6;
	private int shardingNum = 8;
	private String dbName;
	
	
	@Override
	public long generateKeys (int userId, String sequenceName, int step) {
		BaseDAO dao = getQueryDAO(userId);
		return dao.generateKeys(sequenceName, step, dbName);
	}
	
	@Override
	public Long[] getNextKeywordIdBatch(int userId, String sequenceName, int step) {
    	
        if (step <= 0) {
        	return new Long[0];
        }

        long start = generateKeys(userId, sequenceName, step);
        
        Long [] ids = new Long[step];
        for(int i = 0; i < step; i ++ ){
        	ids[i] = start + i;
        }
        
        return ids;
    }
	
}
