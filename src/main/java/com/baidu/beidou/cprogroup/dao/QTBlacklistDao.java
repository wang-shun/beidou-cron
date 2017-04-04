package com.baidu.beidou.cprogroup.dao;

import java.util.List;
import java.util.Map;

public interface QTBlacklistDao {

	/**
	 * 根据userId查询不相关词
	 * @param userId
	 * @return
	 */
	public List<Long> findBlackListByUser(Integer userId);
	
	/**
	 * 获取对全部用户生效的黑名单词和灰名单词
	 * @return
	 */
	public Map<Long, Integer> findQTBlackList();
}
