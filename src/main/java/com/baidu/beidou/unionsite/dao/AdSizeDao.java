/**
 * 2009-4-23 下午05:51:11
 */
package com.baidu.beidou.unionsite.dao;

import java.util.Map;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public interface AdSizeDao {
	
	Map<Integer, int[]> findAllSize();
	
	Map<Integer, int[]> findSizeBySizeTypes(int[] sizeTypes);
	
	int getMaxSizeId();

}
