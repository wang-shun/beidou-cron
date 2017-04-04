package com.baidu.beidou.shrink.service;
/**
 * sleep 执行任务
 * @author hexiufeng
 *
 */
public interface DelayTask {
	/**
	 * 执行一次sleep
	 * @param count
	 * @return
	 */
	boolean delay(int count);
}
