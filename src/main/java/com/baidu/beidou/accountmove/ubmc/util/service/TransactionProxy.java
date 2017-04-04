package com.baidu.beidou.accountmove.ubmc.util.service;


/**
 * 用于开启事,在该事务中执行task,这样所有操作都在事务内，其他service不必再配置事务拦截
 * @author heixufeng
 *
 */
public interface TransactionProxy {
	public interface TransTask {
		/**
		 * 执行任务
		 */
		void execute();
	}
	/**
	 * 执行任务，自动被包含在事务中
	 * @param task
	 */
	void commitTask(TransTask task);
}
