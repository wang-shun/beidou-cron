package com.baidu.beidou.cprounit.service.syncubmc.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MaterUrlSet {
	private int index;
	private int count;
	private List<String> materUrlList = null;
	
	public MaterUrlSet() {
		index = 0;
		count = 0;
		materUrlList = new ArrayList<String>();
	}
	
	public MaterUrlSet(Set<String> materUrlSet) {
		this.index = 0;
		this.materUrlList = new ArrayList<String>(materUrlSet);
		this.count = materUrlList.size();
	}
	
	public synchronized boolean hasNext() {
		return count > index;
	}
	
	public synchronized String getNextMaterUrl() {
		if (count > index) {
			return materUrlList.get(index++);
		}
		
		return null;
	}
}
