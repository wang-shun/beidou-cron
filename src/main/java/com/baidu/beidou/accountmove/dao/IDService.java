package com.baidu.beidou.accountmove.dao;

/**
 *  used for primary generate
 * @author work
 *
 */
public interface IDService {

	/**
	 * get primary key from sequence
	 * @param userId userId
	 * @param sequenceName sequenceName
	 * @param step step
	 * @return primary key
	 */
	long generateKeys(int userId, String sequenceName, int step);
	
	
	/**
	 * copy ubmc material,and give new mcid and new version
	 * @param userId
	 * @param mcId
	 * @param version
	 * @return
	 */
	UbmcMaterial generateUbmcMaterial(int userId, long mcId, int version);
	
	/**
	 * 
	 * @param userId
	 * @param mcId
	 * @param version
	 * @param srcGroupId
	 * @param destGroupId
	 * @return
	 */
	UbmcMaterial generateGroupUbmcMaterial(int userId, long mcId, int version, int srcGroupId, int destGroupId);
	
	/**
	 * batch get id
	 * @param userId
	 * @param sequenceName
	 * @param step
	 * @return id array
	 */
	Long[] getNextKeywordIdBatch(int userId, String sequenceName, int step);
}
