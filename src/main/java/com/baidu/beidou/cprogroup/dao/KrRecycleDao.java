package com.baidu.beidou.cprogroup.dao;

import java.util.List;

/**
 * Kr回收站DAO
 * @author qianlei
 *
 */

public interface KrRecycleDao {
	/**
	 * 获取用户回收站中所有词的wordId
	 * @param userId
	 * @return
	 */
	public List<Long> getUserRecycleWordIds(Integer userId);

}
