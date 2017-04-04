package com.baidu.beidou.shrink.service;

/**
 * 用于开启事,在该事务中执行task,这样所有操作都在事务内，其他service不必再配置事务拦截
 * @author work
 *
 */
public interface SimpleTransProxy {
	/**
	 * 执行任务，自动被包含在事务中
	 * @param task
	 */
	void commitTask(TransTask task);
//	void commitTask(Integer userId,TransTask task);
}
