/**
 * 2009-8-20 下午02:29:53
 * @author zengyunfeng
 */
package com.baidu.beidou.util;

/**
 * @author zengyunfeng
 *
 */
public interface ServiceSelector<T> {
	public T invoke(boolean errorExit);
}
