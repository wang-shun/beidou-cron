/**
 * 
 */
package com.baidu.beidou.cache.dao;

import java.util.List;

/**
 * DAO related to 'beidou' database
 * 
 * @author wangqiang04
 * 
 */
public interface BeidouDao {
	/**
	 * Find all user ids
	 * 
	 * @return
	 */
	List<Integer> findAllUserids();
}
