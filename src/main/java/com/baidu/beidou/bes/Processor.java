/**
 * Processor.java 
 */
package com.baidu.beidou.bes;

import java.util.concurrent.TimeUnit;


/**
 * 处理器接口
 * 
 * @author lixukun
 * @date 2014-01-23
 */
public interface Processor<T> {
	/**
	 * 处理数据
	 * @param unit
	 */
	void process(T unit);
	
	/**
	 * 设置后继处理器
	 * @param processor
	 */
	void setSuccessor(Processor processor);
	
	/**
	 * 获得后继处理器 
	 * @return
	 */
	Processor getSuccessor();
	
	/**
	 * 关闭
	 */
	void shutdown();
	
	/**
	 * 等待处理器结束
	 * 
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}
