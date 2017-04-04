package com.baidu.beidou.shrink.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.shrink.service.ShrinkApplyService.ApplyResult;
import com.baidu.beidou.shrink.service.ShrinkMonitor;

public class ShrinkMonitorImpl implements ShrinkMonitor {
	private static final Log LOG = LogFactory.getLog(ShrinkMonitorImpl.class);
	
	private final static int maxMinCount = 15;
	private List<MonitorItem> monitor = new ArrayList<MonitorItem>(maxMinCount);
	
	private long lastMonitorTime = 0;
	private long lastOutTime = 0;
	private long lastMi = 0;
	private final long oneMin = 60000;
	
	private long allRealCount = 0;
	private long allVCount = 0;
	private long groupCount = 0;
	static class MonitorItem{
		MonitorItem (int realCount,int vCount){
			this.realCount = realCount;
			this.vCount = vCount;
		}
		int realCount;
		int vCount;
	}
	@Override
	public void accept(ApplyResult affect) {
		allRealCount += affect.getAffectRealRows();
		allVCount += affect.getAffectVirtualRows();
		groupCount++;
		long now = System.currentTimeMillis();
		if(lastMonitorTime == 0){
			lastMonitorTime = now;
		}
		long index =(now - lastMonitorTime)/oneMin;
		if(index > maxMinCount-1){
			if(index > lastMi){
				MonitorItem itm = monitor.get(0);
				monitor.remove(0);
				monitor.add(itm);
				itm.realCount = affect.getAffectRealRows();
				itm.vCount = affect.getAffectVirtualRows();
			}else{
				monitor.get(maxMinCount-1).realCount+=affect.getAffectRealRows();
				monitor.get(maxMinCount-1).vCount+=affect.getAffectVirtualRows();
			}
		}else{
			if(monitor.size()<= index){
				monitor.add(new MonitorItem(affect.getAffectRealRows(),affect.getAffectVirtualRows()));
			}else{
				monitor.get((int)index).realCount += affect.getAffectRealRows();
				monitor.get((int)index).vCount += affect.getAffectVirtualRows();
			}
		}
		lastMi = index;
	}

	@Override
	public boolean output(String name) {
		return output(name,false);
	}
	@Override
	public boolean output(String name,boolean force) {
		if(monitor.size() == 0)
			return false;
		long now = System.currentTimeMillis();
		if(lastOutTime == 0){
			lastOutTime = now;
			if(!force)
				return false;
		}
		long v = now - lastOutTime;
		StringBuilder sb = new StringBuilder(256);
		if(force){
			sb.append("force ");
		}
		sb.append("output performence(5m,10m,15m;rows,groups,vrows)[");
		sb.append(name);
		sb.append("]:");
		
		if(v >= oneMin * 2 || force){
			// output
			int mc = Math.min(monitor.size(), 5);
			int count = 0;
			for(int i = 0; i < mc;i++){
				count += monitor.get(i).realCount;
			}
			sb.append(count/mc);
			sb.append(",");
			
			mc = Math.min(monitor.size(), 10);
			count = 0;
			for(int i = 0; i < mc;i++){
				count += monitor.get(i).realCount;
			}
			sb.append(count/mc);
			sb.append(",");
			
			mc = Math.min(monitor.size(), 15);
			count = 0;
			for(int i = 0; i < mc;i++){
				count += monitor.get(i).realCount;
			}
			sb.append(count/mc);
			
			sb.append(";{");
			sb.append(this.allRealCount);
			sb.append(",");
			sb.append(this.groupCount);
			sb.append(",");
			sb.append(this.allVCount);
			sb.append("}");
			LOG.info(sb.toString());
			
			lastOutTime = now;
			return true;
		}
		return false;
	}
}
