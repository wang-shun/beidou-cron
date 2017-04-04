/**
 * 
 */
package com.baidu.beidou.util.partition.strategy.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.exception.ConfigureException;
import com.baidu.beidou.util.partition.PartID;
import com.baidu.beidou.util.partition.PartKey;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;

/**
 * 根据key进行范围和模进行双重hash的拆表策略
 * 
 * @author zengyunfeng
 * @version 1.0.0
 */
public class HashPartitionStrategy implements PartitionStrategy {

	private final int mod;
	private List<Long> range = null;
	private final String tablename;
	private final String poname;
	private final static Log log = LogFactory
			.getLog(HashPartitionStrategy.class);

	
	public boolean isEmpty(String value){
		if(value==null||value.length()==0){
			return true;
		}
		return false;
	}
	public HashPartitionStrategy(String tableName, String poName, int mod,
			String range) throws ConfigureException {
		if (mod <= 0) {
			log.warn("分表的模数配置有误");
			this.mod = 8;
		} else {
			this.mod = mod;
		}
		if (isEmpty(tableName) || isEmpty(poName)) {
			ConfigureException exception = new ConfigureException(
					"拆表的表明和实体名没有定义");
			log.error("拆表的表明和实体名没有定义", exception);
		}
		this.tablename = tableName;
		this.poname = poName;
		
		
		String[] ranges = range.split(",");
		Set<Long> rangeSet = new HashSet<Long>(ranges.length);
		Long curValue = null;
		try {
			for(String r : ranges){
				curValue = Long.valueOf(r);
				rangeSet.add(curValue);
				
			}
		} catch (NumberFormatException e1) {
			log.error("拆表的范围不是数字，使用默认值100000", e1);
			rangeSet.clear();
			rangeSet.add(Long.valueOf(100000));
		}
		
		// 排序。
		this.range = new ArrayList<Long>(rangeSet);
		Collections.sort(this.range);

		// 去除非正数
		for (int index = 0; index < this.range.size();) {
			if (this.range.get(index).longValue() <= Long.valueOf(0)) {
				this.range.remove(index);
			} else {
				break;
			}
		}
	}

	
	/**
	 * 根据key，获得其拆表的ID
	 * @param key 拆表的key
	 * @return 返回的拆表ID，如果为null,则表示没有对应的子表
	 */
	public PartID getPartitions(PartKey key) {
		PartID part = new PartID();
		int id = 0;
		for (int index =0; index<range.size(); index++) {
			if (key.getKey() <= range.get(index).longValue()) {
				//根据key的值获得对应的hash值。
				id =(int) key.getKey()%mod+index*mod;
				part.setId(id);
				part.setPoname(poname+id);
				part.setTablename(tablename+id);
				return part;
			}
		}
		return null;
	}

	/**
	 * 获得所有的子表ID
	 * @return 返回所有子表
	 */
	public List<PartID> getAllPartitions() {
		List<PartID> parts = new ArrayList<PartID>();
		PartID part = null;
		int id = 0;
		for (int index =0; index<range.size(); index++) {
				//根据key的值获得对应的hash值。
			for(int modIndex =0; modIndex <mod; modIndex++){
				part = new PartID();
				id = modIndex+index*mod;
				part.setId(id);
				part.setPoname(poname+id);
				part.setTablename(tablename+id);
				parts.add(part);
			}
		}
		return parts;
	}

}
