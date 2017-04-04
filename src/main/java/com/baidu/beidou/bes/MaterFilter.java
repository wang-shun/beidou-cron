/**
 * MaterFilter.java 
 */
package com.baidu.beidou.bes;

/**
 * 物料过滤器接口
 * 
 * @author lixukun
 * @date 2013-12-24
 */
public interface MaterFilter {
	
	/**
	 * 过滤物料
	 * @param context
	 * @return
	 */
	void doFilter(MaterContext context);
}
