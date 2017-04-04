/**
 * 
 */
package com.baidu.beidou.cache.dao;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.cache.bo.StatInfo;
import com.baidu.beidou.cache.bo.UserStatInfo;
import com.baidu.beidou.cache.common.GroupKey;
import com.baidu.beidou.cache.common.PlanKey;
import com.baidu.beidou.cache.common.UnitKey;
import com.baidu.beidou.cache.common.UserKey;

/**
 * @author wangqiang04
 * 
 */
public interface ReportDao {
	/**
	 * Get the value related to the specific key
	 * 
	 * @param key
	 * @return
	 */
	String getSysEnvValue(final String key);

	/**
	 * Set the value related to the specific key
	 * 
	 * @param key
	 * @param value
	 */
	void setSysEnvValue(String key, String value);

	/**
	 * @param usiList
	 */
	void persist(List<UserStatInfo> usiList);
	
	/**
	 * @param tableName
	 */
	void truncate(String tableName);

	/**
	 * 备份beidoureport.stat_user_yest表，并删除指定天数以前的表
	 * 
	 * @param days
	 *            只能删除的天数（从昨天算起）
	 */
	void backupStatUserYest(int days);

	/**
	 * @param repo
	 */
	void persistUserStatYest(Map<UserKey, StatInfo> repo);

	/**
	 * @param repo
	 */
	void persistPlan(Map<PlanKey, StatInfo> repo);

	/**
	 * @param repo
	 */
	void persistGroup(Map<GroupKey, StatInfo> repo);

	/**
	 * @param repo
	 */
	void persistUnit(Map<UnitKey, StatInfo> repo);

	/**
	 * @param repo
	 */
	void incrementalUpdateUserAll(Map<UserKey, StatInfo> repo);
	
	/**
	 * @param repo
	 */
	void persistUserRealtimeStat(Map<Integer, UserStatInfo> repo);
}
