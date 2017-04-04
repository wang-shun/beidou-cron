/**
 * 
 */
package com.baidu.beidou.util.partition.strategy;

import java.util.List;

import com.baidu.beidou.util.partition.PartID;
import com.baidu.beidou.util.partition.PartKey;

/**
 * 拆表的策略
 * 
 * @author zengyunfeng
 * @version 1.0.0
 */
public interface PartitionStrategy {

	/**
	 * 根据key，获得其拆表的ID
	 * @param key 拆表的key
	 * @return 返回的拆表ID，如果为null,则表示没有对应的子表
	 */
	public PartID getPartitions(PartKey key);
	
	/**
	 * 获得所有的子表ID
	 * @return 返回所有子表
	 */
	public List<PartID> getAllPartitions();
}
