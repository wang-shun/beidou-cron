package com.baidu.beidou.shrink.service;

import com.baidu.beidou.shrink.service.ShrinkApplyService.ApplyResult;

/**
 * 监控器，监控执行的效率
 * 
 * @author hexiufeng
 *
 */
public interface ShrinkMonitor {
	/**
	 * 记录一次执行影响的数据
	 * @param affect
	 */
	void accept(ApplyResult affect);
	/**
	 * 输出性能信息
	 * @param name
	 * @return
	 */
	boolean output(String name);
	boolean output(String name,boolean force);
}
