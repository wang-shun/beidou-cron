/**
 * MaterTradeFilter.java 
 */
package com.baidu.beidou.bes;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.google.common.collect.Maps;

/**
 * 行业过滤器
 * 
 * @author lixukun
 * @date 2014-03-23
 */
public class MaterTradeFilter implements MaterFilter {
	private List<MaterTradeRange> ranges;
	
	@Override
	public void doFilter(MaterContext context) {
		TreeMap<Integer, MaterTradeRange> dict = buildDictionary();
		
		List<UnitMaterView> maters = context.getUnitMaters();
		List<UnitMaterView> filtedMaters = new ArrayList<UnitMaterView>();
		
		for (UnitMaterView mater : maters) {
			Entry<Integer, MaterTradeRange> entry = dict.floorEntry(mater.getNewAdTradeId());
			if (entry == null) {
				filtedMaters.add(mater);
				continue;
			}
			
			if (!entry.getValue().inRange(mater.getNewAdTradeId())) {
				filtedMaters.add(mater);
			}
		}
		
		context.setUnitMaters(filtedMaters);
	}
	
	private TreeMap<Integer, MaterTradeRange> buildDictionary() {
		TreeMap<Integer, MaterTradeRange> dict = Maps.newTreeMap();
		if (CollectionUtils.isEmpty(ranges)) {
			return dict;
		}
		
		for (MaterTradeRange range : ranges) {
			dict.put(range.getMinTradeId(), range);
		}
		
		return dict;
	}

	/**
	 * @return the ranges
	 */
	public List<MaterTradeRange> getRanges() {
		return ranges;
	}

	/**
	 * @param ranges the ranges to set
	 */
	public void setRanges(List<MaterTradeRange> ranges) {
		this.ranges = ranges;
	}

}
