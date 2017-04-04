/**
 * TaskContext.java 
 */
package com.baidu.beidou.bes;

/**
 * 执行任务时的上下文类，用于记录任务的一些基本信息，放在taskMonitorMap中
 * 
 * @author lixukun
 * @date 2013-12-26
 */
public class TaskContext {
	private Thread thread;
	private int status;
	private String errorMsg;
	private long startTime;
	
	public TaskContext() {
		status = 0;
		startTime = System.currentTimeMillis();
	}
	
	public void setStatus(int status, String errMsg) {
		this.status = status;
		this.errorMsg = errMsg;
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getErrMsg() {
		return errorMsg;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(status).append("|")
		  .append(errorMsg).append("|")
		  .append("used ").append(System.currentTimeMillis() - startTime).append("ms");
		
		return sb.toString();
	}
}
