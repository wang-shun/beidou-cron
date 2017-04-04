/**
 * 2009-4-22 上午03:21:56
 */
package com.baidu.beidou.unionsite.service;

import java.io.IOException;

/**
 * @author zengyunfeng
 * @version 1.0.7
 * Q值文件处理接口
 */
public interface QValueService {

	/**
	 * 获得Q值的二进制配置文件，如果配置文件发生变化，则排序，过滤，处理，然后以二进制文件存储，如果文件没有发生变化，则直接返回上次保存的二进制文件
	 * 
	 * @author zengyunfeng
	 * @param q_main
	 * @param q_site
	 * @param recompute 是否需要重新载入Q值文件
	 * @return 返回Q值二进制文件名
	 * @throws IOException
	 */
	String loadQValue(String q_main, String q_site, boolean recompute)throws IOException;
}
