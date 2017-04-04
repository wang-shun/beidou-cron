package com.baidu.beidou.shrink.service;

/**
 * 执行任务，这个任务放在SimpleTransProxy执行会被自动放入事务中
 * @author hexiufeng
 *
 */
public interface TransTask {
	/**
	 * 执行任务
	 */
	void execute();
}
