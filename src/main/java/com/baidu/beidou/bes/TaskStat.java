/**
 * TaskStat.java 
 */
package com.baidu.beidou.bes;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task执行的一个统计类<br/>
 * 目前只统计了任务数和执行时间，后续在完善优化时可以加入更多维度的数据统计<br/>
 * 
 * @author lixukun
 * @date 2013-12-26
 */
public class TaskStat {
	private AtomicInteger taskCount;
	private volatile long maxExecTime;
	private volatile long minExecTime;
	private AtomicInteger errorCount;
	
	public TaskStat() {
		taskCount = new AtomicInteger();
		maxExecTime = 0;
		minExecTime = Long.MAX_VALUE;
		errorCount = new AtomicInteger();
	}
	
	public void submitContext(TaskContext context) {
		if (context == null) {
			return;
		}
		
		taskCount.incrementAndGet();
		if (context.getStatus() != 0) {
			errorCount.incrementAndGet();
		}
		
		long usedTime = System.currentTimeMillis() - context.getStartTime();
		if (maxExecTime < usedTime) {
			maxExecTime = usedTime;
		}
		
		if (minExecTime > usedTime) {
			minExecTime = usedTime;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[total:").append(taskCount.get()).append("]")
		  .append("[err:").append(errorCount.get()).append("]")
		  .append("[max:").append(maxExecTime).append("ms]")
		  .append("[min:").append(minExecTime).append("ms]");
		
		return sb.toString();
	}
}
